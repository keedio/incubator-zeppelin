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
package org.apache.zeppelin.display;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * This class implements user defined functions
 * @author rolmo
 *
 */
public class Utils {

  /**
   * Just a sample expression
   * @param att1 att
   * @param att2 att
   * @return Sum
   */
  public static int suma(int att1, int att2) {
    return att1 + att2;
  }

  /**
   * Just a sample expression
   * @return formatted actual date
   */
  public static String now() {
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    return sdf.format(new Date());
  }

}
