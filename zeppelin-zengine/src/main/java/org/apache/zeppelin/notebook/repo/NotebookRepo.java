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

import java.io.IOException;
import java.util.List;

import org.apache.zeppelin.notebook.Note;
import org.apache.zeppelin.notebook.NoteInfo;

/**
 * Notebook repository (persistence layer) abstraction
 */
public interface NotebookRepo {
  public List<NoteInfo> list() throws IOException;
  public List<NoteInfo> list(String owner) throws IOException;
  public Note get(String noteId, String owner) throws IOException;
  public void save(Note note) throws IOException;
  public void remove(String noteId, String owner) throws IOException;
  boolean share(String noteId, String owner, String newOwner) throws IOException;
  boolean revokeShare(String noteId, String owner) throws IOException;
  String kickOut(String noteId, String owner, String oldOwner) throws IOException;
  public List<NoteInfo> listShared(String owner) throws IOException;
}
