/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2007, 2008, 2009, 2010, 2011 Zimbra, Inc.
 *
 * The contents of this file are subject to the Zimbra Public License
 * Version 1.3 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.cs.imap;

import java.io.IOException;

final class NioImapRequest extends ImapRequest {
    private Literal literal;    // current literal data
    private int literalCount;   // remaining byte count for current literal
    private boolean complete;   // if true then request is complete

    NioImapRequest(ImapHandler handler) {
        super(handler);
    }

    boolean parse(Object obj) throws IOException {
        if (literal != null) {
            parseLiteral((byte[]) obj);
        } else {
            parseCommand((String) obj);
        }
        return complete;
    }

    private void parseLiteral(byte[] b) throws IOException {
        assert b.length <= literalCount;
        literalCount -= literal.put(b, 0, b.length);
        if (literalCount <= 0) {
            assert literal.remaining() == 0;
            addPart(literal);
            literal = null;
        }
    }

    private void parseCommand(String line) throws IOException {
        addPart(line);
        LiteralInfo li = LiteralInfo.parse(line); // literal format is already validated in decoder
        if (li != null) {
            literalCount = li.getCount();
            literal = Literal.newInstance(literalCount, isAppend());
            if (li.count <= 0) { // empty literal
                addPart(literal);
                complete = true;
            } else if (li.isBlocking()) {
                mHandler.sendContinuation("send literal data");
            }
        } else {
            complete = true;
        }
    }

    @Override
    public Literal readLiteral() throws ImapParseException {
        skipChar('{');
        if (mIndex + 1 >= mParts.size()) {
            throw new ImapParseException(mTag, "no next literal");
        }
        Part part = mParts.get(mIndex + 1);
        mIndex += 2;
        mOffset = 0;
        return part.getLiteral();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Part part : mParts) {
            sb.append(part);
            if (part.isString()) sb.append("\r\n");
        }
        return sb.toString();
    }
}
