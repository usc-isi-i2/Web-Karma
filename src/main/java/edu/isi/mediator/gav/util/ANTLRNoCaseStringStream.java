/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *  
 *    This code was developed by the Information Integration Group as part 
 *    of the Karma project at the Information Sciences Institute of the 
 *    University of Southern California.  For more information, publications, 
 *    and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
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


package edu.isi.mediator.gav.util;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;

/**
 * Matches case insensitive strings.
 *
 * @author The OGSA-DAI Project Team
 */
public class ANTLRNoCaseStringStream  extends ANTLRStringStream 
{
    
   
    /**
     * Constructor.
     * @param string
     */
    public ANTLRNoCaseStringStream(String string) 
    {
        super(string);
    }

    /** Interface implementation */
    public int LA(int i) 
    {
        int r = i;
        if ( r==0 ) {
            return 0; // undefined
        }
        if ( r<0 ) {
            r++; // e.g., translate LA(-1) to use offset 0
        }

        if ( (p+r-1) >= n ) {

            return CharStream.EOF;
        }
        return Character.toUpperCase(data[p+i-1]);
    }
}
