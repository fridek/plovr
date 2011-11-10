/*
 * Copyright 2007 The Closure Compiler Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.javascript.jscomp;

import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

/**
 * Converts property accesses from quoted string syntax to dot syntax, where
 * possible. Dot syntax is more compact and avoids an object allocation in
 * IE 6.
 *
 */
class ConvertToDottedProperties extends AbstractPostOrderCallback
    implements CompilerPass {

  private final AbstractCompiler compiler;

  ConvertToDottedProperties(AbstractCompiler compiler) {
    this.compiler = compiler;
  }

  @Override
  public void process(Node externs, Node root) {
    NodeTraversal.traverse(compiler, root, this);
  }

  @Override
  public void visit(NodeTraversal t, Node n, Node parent) {
    switch (n.getType()) {
      case Token.GET:
      case Token.SET:
      case Token.STRING:
        if (NodeUtil.isObjectLitKey(n, parent) &&
            NodeUtil.isValidPropertyName(n.getString())) {
          n.putBooleanProp(Node.QUOTED_PROP, false);
        }
        break;

      case Token.GETELEM:
        Node left = n.getFirstChild();
        Node right = left.getNext();
        if (right.isString() &&
            NodeUtil.isValidPropertyName(right.getString())) {
          n.removeChild(left);
          n.removeChild(right);
          parent.replaceChild(n, new Node(Token.GETPROP, left, right));
          compiler.reportCodeChange();
        }
        break;
    }
  }
}
