/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.accumulo.core.util.shell.commands;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.util.shell.Shell;
import org.apache.accumulo.core.util.shell.Shell.Command;
import org.apache.accumulo.core.util.shell.Token;
import org.apache.commons.cli.CommandLine;

public class AuthenticateCommand extends Command {

  private static final Charset utf8 = Charset.forName("UTF8");

  @Override
  public int execute(final String fullCommand, final CommandLine cl, final Shell shellState) throws AccumuloException, AccumuloSecurityException, IOException {
    final String user = cl.getArgs()[0];
    final String p = shellState.readMaskedLine("Enter current password for '" + user + "': ", '*');
    if (p == null) {
      shellState.getReader().printNewline();
      return 0;
    } // user canceled
    final byte[] password = p.getBytes(utf8);
    final boolean valid = shellState.getConnector().securityOperations().authenticateUser(user, password);
    shellState.getReader().printString((valid ? "V" : "Not v") + "alid\n");
    return 0;
  }
  
  @Override
  public String description() {
    return "verifies a user's credentials";
  }
  
  @Override
  public String usage() {
    return getName() + " <username>";
  }
  
  @Override
  public void registerCompletion(final Token root, final Map<Command.CompletionSet,Set<String>> completionSet) {
    registerCompletionForUsers(root, completionSet);
  }
  
  @Override
  public int numArgs() {
    return 1;
  }
}
