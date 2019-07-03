package org.misha.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.UtilityClass;
import org.springframework.context.annotation.Bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.google.common.collect.Maps.newIdentityHashMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@UtilityClass
public class ClassMethods {
    private static final Set<Method> skip = new HashSet<Method>() {{
        addAll(Arrays.asList(Object.class.getMethods()));
    }};

    private static Set<Method> methods(Class<?> c) {
        final Set<Method> methods = new HashSet<>();
        if (c.getSuperclass() != null) {
            methods.addAll(methods(c.getSuperclass()));
        }
        methods.addAll(Arrays.asList(c.getDeclaredMethods()));
        return methods.stream().filter(m -> !skip.contains(m)).collect(toSet());
    }

    public static String describePublic(final Class<?> c) throws JsonProcessingException {
        final Map<String, Set<MethodDescription>> result = newIdentityHashMap();
        result.put(c.getName(), methods(c).stream()
                                          .map(m -> describe().apply(m))
                                          .filter(desc -> desc.modifiers.contains("public"))
                                          .filter(desc -> !desc.name.contains("lambda"))
                                          .collect(toSet()));
        return new GsonBuilder().setPrettyPrinting().create().toJson(result);
    }

    private static Function<Method, MethodDescription> describe() {
        return m -> MethodDescription.builder()
                                     .argTypes(
                                             Arrays.stream(m.getParameterTypes()).map(Class::getName).collect(toList()))
                                     .name(m.getName())
                                     .annotations(Arrays.stream(m.getAnnotations())
                                                        .map(Annotation::toString)
                                                        .collect(toList()))
                                     .returnType(m.getReturnType().getName())
                                     .modifiers(Modifier.toString(m.getModifiers()))
                                     .build();
    }

    @AllArgsConstructor
    @Data
    @Builder
    private static class MethodDescription {
        private String name;
        private String returnType;
        private List<String> argTypes = new ArrayList<>();
        private List<String> annotations = new ArrayList<>();
        private String modifiers;
    }
}
