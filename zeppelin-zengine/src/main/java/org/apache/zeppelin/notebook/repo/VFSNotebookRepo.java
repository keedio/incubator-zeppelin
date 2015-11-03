/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.zeppelin.notebook.repo;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.google.gson.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.*;
import org.apache.zeppelin.conf.ZeppelinConfiguration;
import org.apache.zeppelin.conf.ZeppelinConfiguration.ConfVars;
import org.apache.zeppelin.notebook.Note;
import org.apache.zeppelin.notebook.NoteInfo;
import org.apache.zeppelin.notebook.Paragraph;
import org.apache.zeppelin.scheduler.Job.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
*
*/
public class VFSNotebookRepo implements NotebookRepo {
  Logger logger = LoggerFactory.getLogger(VFSNotebookRepo.class);

  private FileSystemManager fsManager;
  private URI filesystemRoot;

  private ZeppelinConfiguration conf;

  public VFSNotebookRepo(ZeppelinConfiguration conf) throws IOException {
    this.conf = conf;

    try {
      filesystemRoot = new URI(conf.getNotebookDir());
    } catch (URISyntaxException e1) {
      throw new IOException(e1);
    }

    if (filesystemRoot.getScheme() == null) { // it is local path
      try {
        this.filesystemRoot = new URI(new File(
            conf.getRelativeDir(filesystemRoot.getPath())).getAbsolutePath());
      } catch (URISyntaxException e) {
        throw new IOException(e);
      }
    } else {
      this.filesystemRoot = filesystemRoot;
    }
    fsManager = VFS.getManager();
  }

  private String getPath(String path) {
    if (path == null || path.trim().length() == 0) {
      return filesystemRoot.toString();
    }
    if (path.startsWith("/")) {
      return filesystemRoot.toString() + path;
    } else {
      return filesystemRoot.toString() + "/" + path;
    }
  }

  private boolean isDirectory(FileObject fo) throws IOException {
    if (fo == null) return false;
    if (fo.getType() == FileType.FOLDER) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public List<NoteInfo> list(String owner) throws IOException {
    FileObject rootDir = getRootDir(owner);

    FileObject[] children = rootDir.getChildren();

    List<NoteInfo> infos = new LinkedList<NoteInfo>();
    for (FileObject f : children) {
      String fileName = f.getName().getBaseName();
      if (f.isHidden()
        || fileName.startsWith(".")
        || fileName.startsWith("#")
        || fileName.startsWith("~")) {
        // skip hidden, temporary files
        continue;
      }

      if (!isDirectory(f)) {
        // currently single note is saved like, [NOTE_ID]/note.json.
        // so it must be a directory
        continue;
      }

      NoteInfo info = null;

      try {
        info = getNoteInfo(f);
        if (info != null) {
          infos.add(info);
        }
      } catch (IOException e) {
        logger.error("Can't read note " + f.getName().toString(), e);
      }
    }
    infos.addAll(listShared(owner));
    return infos;
  }

  @Override
  public List<NoteInfo> list() throws IOException {
    FileObject rootDir = fsManager.resolveFile(getRootDir(), "users");
    if (!rootDir.exists())
      rootDir.createFolder();

    logger.info(rootDir.getName().getPath());
    FileObject[] children = rootDir.getChildren();

    List<NoteInfo> infos = new LinkedList<>();
    for (FileObject f : children) {
      String owner = f.getName().getBaseName();
      if (f.isHidden()
        || owner.startsWith(".")
        || owner.startsWith("#")
        || owner.startsWith("~")) {
        // skip hidden, temporary files
        continue;
      }
      logger.info("OWNER=" + owner);

      if (!isDirectory(f)) {
        // currently one directory per user saved like, users/[OWNER]/[NOTE_ID]/note.json.
        // so it must be a directory
        continue;
      }

      try {
        List<NoteInfo> ownerInfos = list(owner);
        if (ownerInfos != null) {
          infos.addAll(ownerInfos);
        }
      } catch (IOException e) {
        logger.error("Can't read note " + f.getName().toString(), e);
      }
    }
    return infos;
  }

  private Note getNote(FileObject noteDir) throws IOException {
    if (!isDirectory(noteDir)) {
      throw new IOException(noteDir.getName().toString() + " is not a directory");
    }

    FileObject noteJson = noteDir.resolveFile("note.json", NameScope.CHILD);
    if (!noteJson.exists()) {
      throw new IOException(noteJson.getName().toString() + " not found");
    }

    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.setPrettyPrinting();
    Gson gson = gsonBuilder.create();

    FileContent content = noteJson.getContent();
    InputStream ins = content.getInputStream();
    String json = IOUtils.toString(ins, conf.getString(ConfVars.ZEPPELIN_ENCODING));
    ins.close();

    Note note = gson.fromJson(json, Note.class);
//    note.setReplLoader(replLoader);
//    note.jobListenerFactory = jobListenerFactory;

    for (Paragraph p : note.getParagraphs()) {
      if (p.getStatus() == Status.PENDING || p.getStatus() == Status.RUNNING) {
        p.setStatus(Status.ABORT);
      }
    }

    return note;
  }

  private NoteInfo getNoteInfo(FileObject noteDir) throws IOException {
    Note note = getNote(noteDir);
    return new NoteInfo(note);
  }

  @Override
  public Note get(String noteId, String owner) throws IOException {
    FileObject rootDir = fsManager.resolveFile(getPath("/users/" + owner));
    FileObject noteDir = rootDir.resolveFile(noteId, NameScope.CHILD);

    return getNote(noteDir);
  }

  private FileObject getRootDir(String owner) throws IOException {
    FileObject rootDir = fsManager.resolveFile(getPath(owner != null ? "/users/" + owner : "/"));

    if (!rootDir.exists()) {
      throw new IOException("Root path does not exists");
    }

    if (!isDirectory(rootDir)) {
      throw new IOException("Root path is not a directory");
    }

    return rootDir;
  }

  private FileObject getRootDir() throws IOException {
    return getRootDir(null);
  }

  @Override
  public void save(Note note) throws IOException {
    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.setPrettyPrinting();
    Gson gson = gsonBuilder.create();
    String json = gson.toJson(note);
    FileObject rootDir = getRootDir();

    rootDir.createFolder();

    FileObject usersDir = fsManager.resolveFile(rootDir, "users");
    if (!usersDir.exists())
      usersDir.createFolder();

    FileObject ownerDir = fsManager.resolveFile(usersDir, note.getOwner());
    if (!ownerDir.exists())
      ownerDir.createFolder();

    FileObject noteDir = ownerDir.resolveFile(note.id(), NameScope.CHILD);

    if (!noteDir.exists()) {
      noteDir.createFolder();
    }
    if (!isDirectory(noteDir)) {
      throw new IOException(noteDir.getName().toString() + " is not a directory");
    }

    FileObject noteJson = noteDir.resolveFile("note.json", NameScope.CHILD);
    // false means not appending. creates file if not exists
    OutputStream out = noteJson.getContent().getOutputStream(false);
    out.write(json.getBytes(conf.getString(ConfVars.ZEPPELIN_ENCODING)));
    out.close();
  }

  @Override
  public void remove(String noteId, String owner) throws IOException {
    FileObject rootDir = fsManager.resolveFile(getPath("/users/" + owner));
    FileObject noteDir = rootDir.resolveFile(noteId, NameScope.CHILD);

    if (!noteDir.exists()) {
      // nothing to do
      return;
    }

    if (!isDirectory(noteDir)) {
      // it is not look like zeppelin note savings
      throw new IOException("Can not remove " + noteDir.getName().toString());
    }

    noteDir.delete(Selectors.SELECT_SELF_AND_CHILDREN);
  }

  /**
   * Get list of noteinfo shared with owner, excluding its own notes.
   * @param owner
   * @return
   * @throws IOException
   */
  @Override
  public List<NoteInfo> listShared(String owner) throws IOException{
    List<NoteInfo> infos = new LinkedList<>();
    FileObject rootDir = fsManager.resolveFile(getRootDir(), "users");
    if (!rootDir.exists())
      rootDir.createFolder();
    logger.info(rootDir.getName().getPath());

    FileObject[] users = rootDir.getChildren();
    for (FileObject user : users) {
      if (!isDirectory(user)) {
        // currently one directory per user saved like, users/[OWNER]/[NOTE_ID]/note.json.
        // so it must be a directory
        continue;
      }
      //exclude created owner's notes.
      if (user.getName().getBaseName().equals(owner)){
        continue;
      }

      FileObject[] notes = user.getChildren();
      for (FileObject note : notes) {
        NoteInfo info = null;
        try {
          info = getNoteInfo(note);
          if (info.isShared() && info.getOwners().contains(owner)) {
            infos.add(info);
          } else {
            continue;
          }
        } catch (IOException e) {
          logger.error("Can't read note " + note.getName().toString(), e);
        }
      } //end of notes
    } //end of users
    return infos;
  }

  /**
   * Get json from note and set value to shared via aux method 'setValueShared'.
   * Add new owner for the note.
   * @param noteId
   * @param owner
   * @param newOwner
   * @return
   * @throws IOException
   */
  @Override
  public boolean share(String noteId, String owner, String newOwner) throws IOException {
    FileObject noteJson = getNoteJson(noteId, owner);
    addNewOwner(noteJson, newOwner);
    return setValueShared(noteJson, true);
  }

  /**
   * Get json from note and set value to not shared (false) via aux method 'setValueShared'.
   * Owner's list is keept.
   * @param noteId
   * @param owner
   * @return
   * @throws IOException
   */
  @Override
  public boolean revokeShare(String noteId, String owner) throws IOException {
    FileObject noteJson = getNoteJson(noteId, owner);
    removeOwners(noteJson);
    return setValueShared(noteJson, false);
  }

  /**
   * Remove one single owner from note's list owners.
   * @param noteId
   * @param owner
   * @param ownerToRemove
   * @return
   * @throws IOException
   */
  @Override
  public String kickOut(String noteId, String owner, String ownerToRemove) throws IOException {
    FileObject noteJson = getNoteJson(noteId, owner);
    return removeSingleOwner(noteJson, ownerToRemove);
  }

  /**
   * Aux method for share:  Get note.json's key called isShared and change boolean value.
   * @param noteJson
   * @param value
   * @return
   * @throws IOException
   */
  private boolean setValueShared(FileObject noteJson, boolean value) throws IOException{
    JsonParser gsonParser = new JsonParser();
    Path path = Paths.get(noteJson.getName().getPath());
    JsonElement jsonElement = gsonParser.parse(new String(Files.readAllBytes(path)));
    // Update json document
    JsonObject jsonObject = jsonElement.getAsJsonObject();
    for (Map.Entry<String, JsonElement> entry: jsonObject.entrySet()) {
      if(entry.getKey().equals("shared")) {
        entry.setValue(gsonParser.parse(String.valueOf(value)));
        break;
      }
    }
    // serialize note.json
    OutputStream out = noteJson.getContent().getOutputStream(false);
    out.write(jsonElement.getAsJsonObject().toString().getBytes(conf.getString(ConfVars.ZEPPELIN_ENCODING)));
    out.close();
    return gsonParser.parse(new String(Files.readAllBytes(path))).getAsJsonObject().get("shared").getAsBoolean();
  }

  /**
   * Aux method for share: get note.json's key called owners and add a new one.
   * @param noteJson
   * @param newOwner
   * @return
   * @throws IOException
   */
  private String addNewOwner(FileObject noteJson, String newOwner) throws IOException {
    JsonParser gsonParser = new JsonParser();
    Path path = Paths.get(noteJson.getName().getPath());
    JsonElement jsonElement = gsonParser.parse(new String(Files.readAllBytes(path)));

    // Update json document
    jsonElement.getAsJsonObject().getAsJsonArray("owners").add(gsonParser.parse(newOwner));
    // serialize note.json
    OutputStream out = noteJson.getContent().getOutputStream(false);
    out.write(jsonElement.getAsJsonObject().toString().getBytes(conf.getString(ConfVars.ZEPPELIN_ENCODING)));
    out.close();
    return newOwner;
  }

  /**
   * Aux method for revokeShare: remove all elements from owners's list except creator's note.
   * @param noteJson
   * @return
   * @throws IOException
   */
  private boolean removeOwners(FileObject noteJson) throws IOException {
    JsonParser gsonParser = new JsonParser();
    Path path = Paths.get(noteJson.getName().getPath());
    JsonElement jsonElement = gsonParser.parse(new String(Files.readAllBytes(path)));
    JsonObject jsonObject = jsonElement.getAsJsonObject();
    int size = 0;
    boolean removed = false;
    for (Map.Entry<String, JsonElement> entry: jsonObject.entrySet()) {
      if(entry.getKey().equals("owners")) {
        JsonArray jsonOwners = entry.getValue().getAsJsonArray();
        Iterator<JsonElement> iterator = jsonOwners.iterator();
        while(iterator.hasNext()){
            iterator.next();
            iterator.remove();
        }
        size = jsonOwners.size();
        break; //stop when find key: owners
      }
    }
    if (size == 0) {
      OutputStream out = noteJson.getContent().getOutputStream(false);
      out.write(jsonElement.getAsJsonObject().toString().getBytes(conf.getString(ConfVars.ZEPPELIN_ENCODING)));
      out.close();
      removed = true;
    }
    return removed;
  }

  /**
   * Remove a single user from owners's list in note.json
   * @param noteJson
   * @param ownerToRemove
   * @throws IOException
   */
  private String removeSingleOwner(FileObject noteJson, String ownerToRemove) throws IOException {
    JsonParser gsonParser = new JsonParser();
    Path path = Paths.get(noteJson.getName().getPath());
    JsonElement jsonElement = gsonParser.parse(new String(Files.readAllBytes(path)));
    JsonObject jsonObject = jsonElement.getAsJsonObject();
    String ownerRemoved = "";
    for (Map.Entry<String, JsonElement> entry: jsonObject.entrySet()) {
      if(entry.getKey().equals("owners")) {
        JsonArray jsonOwners = entry.getValue().getAsJsonArray();
        Iterator<JsonElement> iterator = jsonOwners.iterator();
        while(iterator.hasNext()){
          JsonElement owner = iterator.next();
          if (owner.getAsString().equals(ownerToRemove)) {
            iterator.remove();
            ownerRemoved = owner.getAsString();
            break;
          }
        }
        break;
      }
    }
    OutputStream out = noteJson.getContent().getOutputStream(false);
    out.write(jsonElement.getAsJsonObject().toString().getBytes(conf.getString(ConfVars.ZEPPELIN_ENCODING)));
    out.close();
    return ownerRemoved;
  }

  /**
   * Get note.json allocated in user directory as a FileObject.
   * @param noteId
   * @param owner
   * @return
   * @throws IOException
   */
  private FileObject getNoteJson(String noteId, String owner) throws IOException{
      FileObject rootDir = fsManager.resolveFile(getPath("/users/" + owner));
      FileObject noteDir = rootDir.resolveFile(noteId, NameScope.CHILD);
      FileObject noteJson = noteDir.resolveFile("note.json", NameScope.CHILD);
      return noteJson;
  }

}
