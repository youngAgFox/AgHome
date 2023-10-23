package com.ag.database;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.ag.TransmissionHelper;

import util.StringUtil;

public class ParameterHelper {

    private static final IntConvertor intConvertor = new IntConvertor();
    private static final BooleanConvertor booleanConvertor = new BooleanConvertor();
    private static final LongConvertor longConvertor = new LongConvertor();
    private static final DoubleConvertor doubleConvertor = new DoubleConvertor();
    private static final StringConvertor stringConvertor = new StringConvertor();

    public static interface Convertor<A,B> {
        B convert(A object);
    }

    private static class IntConvertor implements Convertor<String, Integer> {
        @Override
        public Integer convert(String object) {
            return Integer.parseInt(object);
        }
    }

    private static class BooleanConvertor implements Convertor<String, Boolean> {
        @Override
        public Boolean convert(String object) {
            return Boolean.parseBoolean(object);
        }
    }

    private static class LongConvertor implements Convertor<String, Long> {
        @Override
        public Long convert(String object) {
            return Long.parseLong(object);
        }
    }

    private static class DoubleConvertor implements Convertor<String, Double> {
        @Override
        public Double convert(String object) {
            return Double.parseDouble(object);
        }
    }

    private static class StringConvertor implements Convertor<String, String> {
        @Override
        public String convert(String object) {
            return object;
        }
    }

    private static class ListConvertor<T> implements Convertor<String, List<T>> {

        private final Convertor<String, T> elementConvertor;

        public ListConvertor(Convertor<String, T> elementConvertor) {
            this.elementConvertor = elementConvertor;
        }

        @Override
        public List<T> convert(String object) {
            if (!object.startsWith("[") || !object.endsWith("]")) {
                throw new IllegalArgumentException("Passed in string does not present as a list. "
                    + "List string must be enclosed by square brackets '[]'");
            }

            // trim enclosing brackets
            object = object.substring(1, object.length() - 1);

            return StringUtil.parseCommaDelimitedList(object).stream()
                .map(elementConvertor::convert)
                .collect(Collectors.toList());
        }

    }

    private static class MapConvertor<K,V> implements Convertor<String, Map<K,V>> {

        private final Convertor<String, K> keyConvertor;
        private final Convertor<String, V> valueConvertor;

        public MapConvertor(Convertor<String, K> keyConvertor, Convertor<String, V> valueConvertor) {
            this.keyConvertor = keyConvertor;
            this.valueConvertor = valueConvertor;
        }

        @Override
        public Map<K, V> convert(String object) {
            // Exp format is {"'key':'value'","'key2':'value2'"}
            if (!object.startsWith("[") || !object.endsWith("]")) {
                throw new IllegalArgumentException("Passed in string does not present as a map. "
                    + "Map string must be enclosed by curly brackets '{}'");
            }

            // trim enclosing brackets
            object = object.substring(1, object.length() - 1);

            Map<K, V> map = new HashMap<>();
            List<String> keyValuePairs = StringUtil.parseCommaDelimitedList(object);
            for (String keyValuePair : keyValuePairs) {
                List<String> elements = StringUtil.parseDelimitedList(keyValuePair, ':');
                if (2 != elements.size()) {
                    throw new IllegalArgumentException("key value pair with ':' delimiter has incorrect number of elements: " + elements.size());
                }
                map.put(keyConvertor.convert(elements.get(0)), valueConvertor.convert(elements.get(1)));
            }
            return map;
        }
    }

    public static <S extends Storable> void validateUniqueParameter(String message, Storer<S> storer,
            Predicate<S> matches) {

        try {
            if (storer.contains(matches)) {
                throw new InvalidParameterException(message);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to validate uniqueness (" + message + ")", e);
        }
    }

    public static void validateParametersAreNotNull(Map<String, String> params, String... paramNames) {
        Set<String> nullParameters = new HashSet<>();
        for (String param : paramNames) {
            if (null == params.get(param)) {
                nullParameters.add(param);
            }
        }
        if (!nullParameters.isEmpty()) {
            throw new InvalidParameterException("Parameters must be non-null: " + nullParameters);
        }
    }

    public static void validateParametersExist(Map<String, String> params, String... paramNames) {
        Set<String> missingParams = new HashSet<>();
        for (String reqParam : paramNames) {
            if (!params.containsKey(reqParam)) {
                missingParams.add(reqParam);
            }
        }
        if (!missingParams.isEmpty()) {
            throw new InvalidParameterException("Parameters must be non-null: " + missingParams);
        }
    }

    public static <T> Map<String, T> mapParameters(Map<String, String> params, Convertor<String, T> convertor,
            String... paramNames) {
        Map<String, T> mappedParameters = new HashMap<>();
        for (String param : paramNames) {
            mappedParameters.put(param, convertor.convert(param));
        }
        return mappedParameters;
    }

    public static Convertor<String, Integer> getIntConvertor() {
        return intConvertor;
    }

    public static Convertor<String, Boolean> getBooleanConvertor() {
        return booleanConvertor;
    }

    public static Convertor<String, Long> getLongConvertor() {
        return longConvertor;
    }

    public static Convertor<String, Double> getDoubleConvertor() {
        return doubleConvertor;
    }

    public static Convertor<String, String> getStringConvertor() {
        return stringConvertor;
    }

    public static <T> Convertor<String, List<T>> getListConvertor(Convertor<String, T> convertor) {
        return new ListConvertor<T>(convertor);
    }

    public static <K,V> Convertor<String, Map<K,V>> getMapConvertor(Convertor<String, K> keyConvertor, Convertor<String, V> valueConvertor) {
        return new MapConvertor<>(keyConvertor, valueConvertor);
    }

    public static void addStringValues(Map<String, String> map, Object ... valuePairs) {
        if (valuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("valuePairs array cannot have odd length");
        }
        for (int i = 0; i < valuePairs.length; i += 2) {
            if (!(valuePairs[i] instanceof String)) {
                throw new IllegalArgumentException("Expected first Object to be a String key");
            }
            String key = (String) valuePairs[i];
            Object valueObj = valuePairs[i + 1];
            String value = TransmissionHelper.toTransmissionString(valueObj);

            map.put(key, value);
        }
    }

    public static Map<String, String> createStringValueMap(Object ... valuePairs) {
        Map<String, String> map = new HashMap<>();
        addStringValues(map, valuePairs);
        return map;
    }

    public static Map<String, String> createStringValueMap(Map<String, Object> properties) {
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            map.put(entry.getKey(), TransmissionHelper.toTransmissionString(entry.getValue()));
        }
        return map;
    }

    public static String getPropertyString(Map<String, Object> properties) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            sb.append("<")
                .append(entry.getValue().getClass().getSimpleName())
                .append(": ")
                .append(entry.getKey())
                .append("> ");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

}
