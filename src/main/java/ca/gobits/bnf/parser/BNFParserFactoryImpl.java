//
// Copyright 2013 Mike Friesen
//
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
//

package ca.gobits.bnf.parser;

import java.util.List;
import java.util.Map;

/**
 * IParser Factory implementation.
 */
public class BNFParserFactoryImpl implements BNFParserFactory {

    /** instance of BNF Sequence Factory. */
    private final ISequenceFactory df = new BNFSequenceFactoryImpl();

    @Override
    public IParser json() {

        Map<String, List<BNFSequence>> map = this.df.json();
        IParser IParser = new BNFParserImpl(map);

        return IParser;
    }

}
