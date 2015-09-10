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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The evaluator utility class. This class should take an expression string 
 * and evaluate using the utility class. This class should defined 
 * statics methods in order to interpret them
 * @author rolmo
 *
 */
public class Evaluator {
  
  Logger LOG = LoggerFactory.getLogger(Evaluator.class);
  Class utilityClass;

  /**
   * Eval passed expression. Should be defined on Utils class 
   * @param command Script comman to execute
   * @return Evaluated expression
   * @throws IllegalAccessException 
   * @throws InstantiationException 
   */
  public Evaluator(String classImpl) throws Exception {
    this.utilityClass = Class.forName(classImpl);
  }
  
  /**
   * Eval the expression passed as argument
   * @param command expression to eval
   * @return
   */
  public Object eval(String command) {

    Object obj = null;
    
    // Check if expression has to be evaluated.
    if (command.indexOf("eval:") < 0) {
      return command;
    }
    
    String expresionToEval = command.substring(5);

    try {
      Map<String, String> mapFQN = getFQN(expresionToEval);
      
      // First, we try with fully qualified name
      JexlEngine jexl = new JexlEngine();
      String expression = "utils." + mapFQN.get("method");
      Expression expr = jexl.createExpression(expression);
      JexlContext jc = new MapContext();
    
      jc.set("utils", Class.forName(mapFQN.get("clazz")));
      obj = expr.evaluate(jc);   
      if (obj != null) 
        return obj;
    } catch (Exception e) {
      LOG.debug("Error trying to use the FQN class.");
    }

    // If it fails, we apply the default utility class
    LOG.debug("Trying the default utility class");
    try {
      JexlEngine jexl = new JexlEngine();
      String expression = "utils." + expresionToEval;
      Expression expr = jexl.createExpression(expression);
      JexlContext jc = new MapContext();
      jc.set("utils", utilityClass);

      obj = expr.evaluate(jc);
    } catch (Exception e) {
      LOG.debug("Error using configured utility class");
    }

    return obj;
  }
  
  private static HashMap<String, String> getFQN(String function) {
    HashMap<String, String> map = new HashMap<>();
    String pattern = "(?<clazz>.+\\..+)\\.(?<method>.+)";
    
    Pattern regex = Pattern.compile(pattern);
    Matcher matcher = regex.matcher(function);
    
    matcher.find();
    
    String matcherClazz = matcher.group("clazz");
    String matcherMethod = matcher.group("method");
    
    map.put("clazz", matcherClazz);
    map.put("method", matcherMethod);
    
    return map;
  }

}
