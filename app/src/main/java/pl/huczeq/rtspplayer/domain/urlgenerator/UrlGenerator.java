package pl.huczeq.rtspplayer.domain.urlgenerator;

import pl.huczeq.rtspplayer.data.model.urltemplates.UrlTemplate;

public class UrlGenerator {

    public static String generate(UrlTemplate urlTemplate, UrlComponentsProvider urlComponentsProvider) {
        if(urlComponentsProvider.usesAuthorization())
            return parseTemplate(urlTemplate, urlTemplate.getUrlTemplateAuth(), urlComponentsProvider);
        return parseTemplate(urlTemplate, urlTemplate.getUrlTemplateNoneAuth(), urlComponentsProvider);
    }

    private static String parseTemplate(UrlTemplate urlTemplate, String url, UrlComponentsProvider urlComponentsProvider) {
        StringBuilder fullUrl = new StringBuilder();
        String bane;
        int posS, posE;
        while((posS = url.indexOf("{")) >= 0) {
            posE = url.indexOf("}");
            if(posE < 0)
                return fullUrl.toString();
            fullUrl.append(url.substring(0, posS));
            bane = url.substring(posS+1, posE);
            fullUrl.append(getValueByComponentName(urlTemplate, bane, urlComponentsProvider));
            url = url.substring(posE+1);
        }
        fullUrl.append(url);
        return fullUrl.toString();
    }

    private static String getValueByComponentName(UrlTemplate urlTemplate, String variable, UrlComponentsProvider urlComponentsProvider) {
        if(variable.equalsIgnoreCase("user")) {
            return urlComponentsProvider.getUserName();
        }else if(variable.equalsIgnoreCase("password")) {
            return urlComponentsProvider.getPassword();
        }else if(variable.equalsIgnoreCase("addressip")) {
            return urlComponentsProvider.getAddressIp();
        }else if(variable.equalsIgnoreCase("port")) {
            return urlComponentsProvider.getPort();
        }else if(variable.equalsIgnoreCase("channel")) {
            return urlComponentsProvider.getChannel();
        }else if(variable.equalsIgnoreCase("stream")) {
            return streamTypeToString(urlTemplate, urlComponentsProvider.getStream());
        }else if(variable.equalsIgnoreCase("serverurl")) {
            return urlComponentsProvider.getServerUrl();
        }else {
            return "";
        }
    }
    private static String streamTypeToString(UrlTemplate urlTemplate, int streamType) {
        if(streamType == UrlTemplate.StreamType.MAIN_STREAM)
            return urlTemplate.getMainStreamValue();
        return urlTemplate.getSubStreamValue();
    }
}
