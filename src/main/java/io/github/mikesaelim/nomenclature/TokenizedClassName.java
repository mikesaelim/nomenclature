package io.github.mikesaelim.nomenclature;

import com.google.common.collect.Lists;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Value
class TokenizedClassName {
    String className;
    List<String> tokens;

    public static TokenizedClassName fromClassName(String className) {
        return new TokenizedClassName(className,
                Lists.newArrayList(StringUtils.splitByCharacterTypeCamelCase(className)));
    }

    public int numTokens() {
        return tokens.size();
    }

    public String getRoot() {
        return tokens.get(numTokens() - 1);
    }

}
