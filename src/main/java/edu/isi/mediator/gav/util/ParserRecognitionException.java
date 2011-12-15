// Copyright (c) The University of Edinburgh, 2008.
//
// LICENCE-START
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// LICENCE-END
//
//modified by MariaMuslea USC/ISI

package edu.isi.mediator.gav.util;

import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.FailedPredicateException;
import org.antlr.runtime.MismatchedRangeException;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.MismatchedTokenException;
import org.antlr.runtime.MismatchedTreeNodeException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;

/**
 * Raised when a parse exception occurs.
 *
 * @author The OGSA-DAI Project Team.
 */

public class ParserRecognitionException 
    extends Exception 
{
	private static final long serialVersionUID = 1L;
	protected String mToken;
    protected int mLine;
    protected int mPositionInLine;
    protected String mErrorID;
    
    private ParserRecognitionException(String errorID)
    {
        super(errorID);
    }

    /**
     * Constructs an instance of a parse exception with the given cause.
     * 
     * @param cause
     *            error cause
     * @return parser exception
     */
    public static ParserRecognitionException getInstance(
            RecognitionException cause)
    {
        String errorID;
        if (cause instanceof NoViableAltException)
        {
            errorID = "NO_VIABLE_ALT_EXCEPTION";
        }
        else if (cause instanceof EarlyExitException)
        {
            errorID = "EARLY_EXIT_EXCEPTION";
        }
        else if (cause instanceof MismatchedRangeException)
        {
            errorID = "MISMATCHED_RANGE_EXCEPTION";
        }
        else if (cause instanceof MismatchedSetException)
        {
            errorID = "MISMATCHED_SET_EXCEPTION";
        }
        else if (cause instanceof MismatchedTokenException)
        {
            errorID = "MISMATCHED_TOKEN_EXCEPTION";
        }
        else if (cause instanceof MismatchedTreeNodeException)
        {
            errorID = "MISMATCHED_TREE_NODE_EXCEPTION";
        }
        else if (cause instanceof FailedPredicateException)
        {
            errorID = "FAILED_PREDICATE_EXCEPTION";
        }
        else
        {
            errorID = "RECOGNITION_EXCEPTION";
        }
        ParserRecognitionException result = new ParserRecognitionException(errorID); 
        result.mToken = cause.token.getText();
        result.mLine = cause.line;
        result.mPositionInLine = cause.charPositionInLine;
        result.mErrorID = errorID;
        
        return result;
    }
    
    public String toString()
    {
        return "Line " + mLine + ":" + mPositionInLine + ": " 
            + mErrorID + " at input '" + mToken + "'";
    }

}
