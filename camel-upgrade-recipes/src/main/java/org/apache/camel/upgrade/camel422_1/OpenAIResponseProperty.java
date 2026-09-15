/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.upgrade.camel422_1;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CAMEL-24539 (camel-openai, Camel 4.22.1): with {@code storeFullResponse=true}, the embeddings,
 * audio-transcription and audio-translation operations moved their full SDK response from the shared
 * {@code CamelOpenAIResponse} exchange property (still used by chat-completion) to an operation-specific
 * property. This class carries the mapping and the Simple-expression rename shared by the Java, XML and
 * YAML DSL visitors.
 */
public final class OpenAIResponseProperty {

    public static final String OLD_PROPERTY_NAME = "CamelOpenAIResponse";
    public static final String OLD_CONSTANT_FIELD = "RESPONSE";
    public static final String OPENAI_CONSTANTS_TYPE = "OpenAIConstants";

    public static final Set<String> ENDPOINT_URI_METHOD_NAMES = Set.of("to", "toD", "enrich", "pollEnrich", "wireTap");

    private static final Map<String, OpenAIResponseProperty> BY_OPERATION = new LinkedHashMap<>();

    static {
        BY_OPERATION.put("embeddings", new OpenAIResponseProperty("CamelOpenAIEmbeddingsResponse", "EMBEDDINGS_RESPONSE"));
        BY_OPERATION.put("audio-transcription",
                new OpenAIResponseProperty("CamelOpenAIAudioTranscriptionResponse", "AUDIO_TRANSCRIPTION_RESPONSE"));
        BY_OPERATION.put("audio-translation",
                new OpenAIResponseProperty("CamelOpenAIAudioTranslationResponse", "AUDIO_TRANSLATION_RESPONSE"));
    }

    private static final Pattern EXCHANGE_PROPERTY_DOT = Pattern.compile("(\\$\\{exchangeProperty\\.)CamelOpenAIResponse([.}])");
    private static final Pattern EXCHANGE_PROPERTY_BRACKET
            = Pattern.compile("(\\$\\{exchangeProperty\\[)CamelOpenAIResponse(\\]\\})");
    private static final Pattern EXCHANGE_PROPERTY_FUNCTION
            = Pattern.compile("(\\$\\{exchangeProperty(?:As)?\\()CamelOpenAIResponse([,)])");

    public final String newPropertyName;
    public final String newConstantField;

    private OpenAIResponseProperty(String newPropertyName, String newConstantField) {
        this.newPropertyName = newPropertyName;
        this.newConstantField = newConstantField;
    }

    /**
     * The replacement property/constant for a route whose single distinct openai operation is
     * {@code operation}, or empty when that operation still uses {@code CamelOpenAIResponse}
     * (chat-completion, responses, tool-execution, audio-speech) or is unknown.
     */
    public static Optional<OpenAIResponseProperty> forOperation(String operation) {
        return Optional.ofNullable(BY_OPERATION.get(operation));
    }

    /**
     * Extracts the operation from an {@code openai:} endpoint URI, e.g.
     * {@code openai:embeddings?storeFullResponse=true} -&gt; {@code embeddings}.
     */
    public static Optional<String> operationFromUri(String uri) {
        if (uri == null || !uri.startsWith("openai:")) {
            return Optional.empty();
        }
        String path = uri.substring("openai:".length());
        int idx = path.indexOf('?');
        return Optional.of(idx >= 0 ? path.substring(0, idx) : path);
    }

    /**
     * Renames {@code CamelOpenAIResponse} in the {@code ${exchangeProperty...}} Simple syntax forms
     * (dotted/OGNL, bracket-index, function call), leaving lookalikes such as
     * {@code CamelOpenAIResponseModel} untouched.
     */
    public String renameSimpleExpression(String value) {
        String replacement = Matcher.quoteReplacement(newPropertyName);
        String renamed = EXCHANGE_PROPERTY_DOT.matcher(value).replaceAll("$1" + replacement + "$2");
        renamed = EXCHANGE_PROPERTY_BRACKET.matcher(renamed).replaceAll("$1" + replacement + "$2");
        renamed = EXCHANGE_PROPERTY_FUNCTION.matcher(renamed).replaceAll("$1" + replacement + "$2");
        return renamed;
    }
}
