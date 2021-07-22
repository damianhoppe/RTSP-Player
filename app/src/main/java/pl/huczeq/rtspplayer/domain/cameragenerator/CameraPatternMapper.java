package pl.huczeq.rtspplayer.domain.cameragenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.huczeq.rtspplayer.data.model.CameraPattern;
import pl.huczeq.rtspplayer.data.model.urltemplates.UrlTemplate;
import pl.huczeq.rtspplayer.domain.model.CameraPatternWithVariables;
import pl.huczeq.rtspplayer.domain.urlgenerator.UrlComponentsProvider;
import pl.huczeq.rtspplayer.domain.urlgenerator.UrlGenerator;
import pl.huczeq.rtspplayer.domain.model.CameraGroupModel;

public class CameraPatternMapper {

    public static final String REGEX_EXPRESSION = "(\\{[^{]+\\})";
    public static final char SPECIAL_VARIABLE_PREFIX = '&';

    private static final List<String> SYSTEM_VARIABLES = List.of("i");

    public static CameraPatternWithVariables toPatternWithVariables(CameraPattern cameraPattern, UrlTemplate urlTemplate) {
        Map<String, String> vars = new HashMap<>();

        CameraPatternWithVariables cameraPatternWithVariables = new CameraPatternWithVariables();
        cameraPatternWithVariables.setName(findVariablesInText(0, cameraPattern.getName(), vars));
        if(cameraPattern.getProducer() != null)
            cameraPatternWithVariables.setProducer(cameraPattern.getProducer());
        if(cameraPattern.getModel() != null) {
            cameraPatternWithVariables.setModel(cameraPattern.getModel());
        }

        if(urlTemplate == null) {
            cameraPatternWithVariables.setUrl(findVariablesInText(0, cameraPattern.getUrl(), vars));
            cameraPatternWithVariables.setUserName(cameraPattern.getUserName());
            cameraPatternWithVariables.setAddressIp(cameraPattern.getAddressIp());
            cameraPatternWithVariables.setPort(cameraPattern.getPort());
            cameraPatternWithVariables.setChannel(cameraPattern.getChannel());
            cameraPatternWithVariables.setServerUrl(cameraPattern.getServerUrl());
            cameraPatternWithVariables.setPassword(cameraPattern.getPassword());
            cameraPatternWithVariables.setStream(cameraPattern.getStream());
        }else {
            cameraPatternWithVariables.setUrl(UrlGenerator.generate(urlTemplate, UrlComponentsProvider.of(cameraPatternWithVariables)));
            cameraPatternWithVariables.setUserName(findVariablesInText(0, cameraPattern.getUserName(), vars));
            cameraPatternWithVariables.setAddressIp(findVariablesInText(0, cameraPattern.getAddressIp(), vars));
            cameraPatternWithVariables.setPort(findVariablesInText(0, cameraPattern.getPort(), vars));
            cameraPatternWithVariables.setChannel(findVariablesInText(0,cameraPattern.getChannel(), vars));
            cameraPatternWithVariables.setServerUrl(findVariablesInText(0,cameraPattern.getServerUrl(), vars));
            cameraPatternWithVariables.setPassword(cameraPattern.getPassword());
            cameraPatternWithVariables.setStream(String.valueOf(cameraPattern.getStream()));
        }
        cameraPatternWithVariables.setVariables(vars);
        return cameraPatternWithVariables;
    }

    private static String findVariablesInText(int fieldId, String text, Map<String, String> vars) {
        if(text == null || text.isEmpty())
            return text;
        Pattern pattern = Pattern.compile(REGEX_EXPRESSION);
        Matcher matcher = pattern.matcher(text);
        int index = 1;
        String newText = text;
        while(matcher.find()) {
            String varContent = matcher.group();
            String varName = null;
            int equalIndex = varContent.indexOf('=');
            if(equalIndex <= 0) {
                continue;
            }
            varName = varContent.substring(1, equalIndex);
            if(isSystemVariable(varName))
                throw new IllegalArgumentException("Trying rewrite " + varName);
            vars.put(varName, varContent.substring(equalIndex+1, varContent.length() - 1));
            newText = newText.replaceFirst(Pattern.quote(varContent), "{" + varName + "}");
        }
        return newText;
    }

    private static int isCorrect(String value) {
        char c;
        for(int i = 0; i < value.length(); i++) {
            c = value.charAt(i);
            if(!Character.isAlphabetic(c) && !Character.isDigit(c))
                return i;
        }
        return -1;
    }

    public static CameraPattern toCameraPattern(CameraGroupModel model) {
        CameraPattern cameraPattern = new CameraPattern();
        cameraPattern.setName(namesUnnamedVariables(model.getName()));
        if(model.getProducer() != null)
            cameraPattern.setProducer(model.getProducer().getName());
        UrlTemplate urlTemplate = null;
        if(model.getModel() != null) {
            cameraPattern.setModel(model.getModel().getName());
            urlTemplate = model.getModel().getUrlTemplate();
        }

        if(urlTemplate == null) {
            cameraPattern.setUrl(namesUnnamedVariables(model.getUrl()));
            cameraPattern.setUserName(model.getUserName());
            cameraPattern.setAddressIp(model.getAddressIp());
            cameraPattern.setPort(model.getPort());
            cameraPattern.setChannel(model.getChannel());
            cameraPattern.setServerUrl(model.getServerUrl());
        }else {
            cameraPattern.setUrl(UrlGenerator.generate(urlTemplate, UrlComponentsProvider.of(cameraPattern)));
            cameraPattern.setUserName(namesUnnamedVariables(model.getUserName()));
            cameraPattern.setAddressIp(namesUnnamedVariables(model.getAddressIp()));
            cameraPattern.setPort(namesUnnamedVariables(model.getPort()));
            cameraPattern.setChannel(namesUnnamedVariables(model.getChannel()));
            cameraPattern.setServerUrl(namesUnnamedVariables(model.getServerUrl()));
        }
        cameraPattern.setPassword(model.getPassword());
        cameraPattern.setStream(String.valueOf(model.getStreamType()));
        return cameraPattern;
    }

    private static String namesUnnamedVariables(String text) {
        if(text == null || text.isEmpty())
            return text;
        Pattern pattern = Pattern.compile(REGEX_EXPRESSION);
        Matcher matcher = pattern.matcher(text);
        int index = 1;
        String newText = text;
        while(matcher.find()) {
            String varContent = matcher.group();
            String varName = null;
            int equalIndex = varContent.indexOf('=');
            if(equalIndex >= 0) {
                varName = varContent.substring(1, equalIndex);
                int incorrectCharIndex = isCorrect(varName);
                if(incorrectCharIndex >= 0)
                    throw new IllegalArgumentException("Found incorrect character " + varName.charAt(incorrectCharIndex) + " at " + incorrectCharIndex + " position.");
                continue;
            }else{
                varName = varContent.substring(1, varContent.length() );
            }
            if(isSystemVariable(varName))
                continue;
            if(varContent.length() <= 4 || Character.isAlphabetic(varContent.charAt(2)))
                continue;
            varName = SPECIAL_VARIABLE_PREFIX + "" + index++;
            String replaceWith = varName + "=" + varContent.substring(1, varContent.length() - 1);
            newText = newText.replaceFirst(Pattern.quote(varContent), "{" + replaceWith + "}");
        }
        return newText;
    }

    public static CameraPattern fillCameraPattern(CameraPattern model, Map<String, String> variables) {
        CameraPattern cameraPattern = new CameraPattern();
        cameraPattern.setName(fillField(model.getName(), variables));
        cameraPattern.setProducer(model.getProducer());
        cameraPattern.setModel(model.getModel());

        cameraPattern.setUserName(fillField(model.getUserName(), variables));
        cameraPattern.setAddressIp(fillField(model.getAddressIp(), variables));
        cameraPattern.setPort(fillField(model.getPort(), variables));
        cameraPattern.setChannel(fillField(model.getChannel(), variables));
        cameraPattern.setServerUrl(fillField(model.getServerUrl(), variables));
        cameraPattern.setPassword(model.getPassword());
        cameraPattern.setStream(String.valueOf(model.getStream()));
        cameraPattern.setUrl(fillField(model.getUrl(), variables));
        return cameraPattern;
    }

    private static String fillField(String text, Map<String, String> variables) {
        if(text == null || text.isEmpty())
            return text;
        Pattern pattern = Pattern.compile(REGEX_EXPRESSION);
        Matcher matcher = pattern.matcher(text);
        int index = 1;
        String newText = text;
        while(matcher.find()) {
            String varContent = matcher.group();
            String varName = null;
            int equalIndex = varContent.indexOf('=');
            if(equalIndex >= 0) {
                varName = varContent.substring(1, equalIndex);
            }else {
                varName = varContent.substring(1,varContent.length()-1);
            }
            String replaceWith = variables.get(varName);
            if(replaceWith != null)
                newText = newText.replaceFirst(Pattern.quote(varContent), replaceWith);
        }
        return newText;
    }

    public static CameraPattern hideSpecialVariableNames(CameraPattern model) {
        CameraPattern cameraPattern = new CameraPattern();
        cameraPattern.setName(hideSpecialVariable(model.getName()));
        cameraPattern.setProducer(model.getProducer());
        cameraPattern.setModel(model.getModel());

        cameraPattern.setUserName(hideSpecialVariable(model.getUserName()));
        cameraPattern.setAddressIp(hideSpecialVariable(model.getAddressIp()));
        cameraPattern.setPort(hideSpecialVariable(model.getPort()));
        cameraPattern.setChannel(hideSpecialVariable(model.getChannel()));
        cameraPattern.setServerUrl(hideSpecialVariable(model.getServerUrl()));
        cameraPattern.setPassword(model.getPassword());
        cameraPattern.setStream(String.valueOf(model.getStream()));
        cameraPattern.setUrl(hideSpecialVariable(model.getUrl()));
        return cameraPattern;
    }

    private static String hideSpecialVariable(String text) {
        if(text == null || text.isEmpty())
            return text;
        Pattern pattern = Pattern.compile(REGEX_EXPRESSION);
        Matcher matcher = pattern.matcher(text);
        int index = 1;
        String newText = text;
        while(matcher.find()) {
            String varContent = matcher.group();
            int equalIndex = varContent.indexOf('=');
            if(equalIndex < 0)
                continue;
            if(varContent.length() < 3)
                continue;
            char firstChar = varContent.charAt(1);
            if(firstChar != SPECIAL_VARIABLE_PREFIX)
                continue;
            if(equalIndex+1 > varContent.length()-1)
                continue;
            String replaceWith = "{" + varContent.substring(equalIndex+1, varContent.length()-1) + "}";
            newText = newText.replaceFirst(Pattern.quote(varContent), replaceWith);
        }
        return newText;
    }

    public static boolean isSystemVariable(String varName) {
        for(String sysytemVariable : SYSTEM_VARIABLES) {
            if ((sysytemVariable).equals(varName)) {
                return true;
            }
        }
        return false;
    }
}
