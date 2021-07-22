package pl.huczeq.rtspplayer.ui.addeditcamera;

import static pl.huczeq.rtspplayer.domain.cameragenerator.CameraPatternMapper.REGEX_EXPRESSION;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.lifecycle.MutableLiveData;

import com.google.common.base.Strings;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.huczeq.rtspplayer.BR;
import pl.huczeq.rtspplayer.data.model.CameraPattern;
import pl.huczeq.rtspplayer.data.model.urltemplates.Model;
import pl.huczeq.rtspplayer.data.model.urltemplates.Producer;
import pl.huczeq.rtspplayer.data.model.urltemplates.UrlTemplate;
import pl.huczeq.rtspplayer.domain.urlgenerator.UrlComponentsProvider;
import pl.huczeq.rtspplayer.domain.urlgenerator.UrlGenerator;
import pl.huczeq.rtspplayer.domain.model.CameraGroupModel;
import pl.huczeq.rtspplayer.util.validation.FieldRules;
import pl.huczeq.rtspplayer.util.validation.RuleWithValueTransform;
import pl.huczeq.rtspplayer.util.validation.interfaces.BasicCondition;
import pl.huczeq.rtspplayer.util.validation.Errors;
import pl.huczeq.rtspplayer.util.validation.FieldValidator;
import pl.huczeq.rtspplayer.util.validation.interfaces.FieldRule;
import pl.huczeq.rtspplayer.util.validation.interfaces.ValueProvider;
import pl.huczeq.rtspplayer.util.validation.interfaces.ValueTransform;

public class CameraFormModel extends BaseObservable {

    private String name;
    private String producerName;
    private String modelName;
    private String userName;
    private String password;
    private String addressIp;
    private String port;
    private String serverUrl;
    private String channel = "0";
    private String url;

    private final BasicCondition isModelNullCondition = new BasicCondition() {
        @Override
        public boolean allows() {
            return model.getValue() != null;
        }
    };

    public static class ExpressionRemoveTransform implements ValueTransform<String> {

        @Override
        public String transform(String value) {
            if(Strings.isNullOrEmpty(value))
                return value;
            Pattern pattern = Pattern.compile(REGEX_EXPRESSION);
            Matcher matcher = pattern.matcher(value);
            String newText = value;
            while(matcher.find()) {
                String varContent = matcher.group();
                newText = newText.replaceFirst(Pattern.quote(varContent), "1");
            }
            return newText;
        }
    }

    private static class ExpressionRuleWrapper extends RuleWithValueTransform {

        private Integer error = null;

        public ExpressionRuleWrapper(FieldRule rule) {
            super(rule);
        }

        public ExpressionRuleWrapper() {
            this(new FieldRule() {
                @Override
                public Integer checkValidity(String text) {
                    return null;
                }
            });
        }

        @Override
        public String transform(String value) {
            if(Strings.isNullOrEmpty(value))
                return value;
            StringBuilder newString = new StringBuilder();
            int index = 0;
            char character;
            boolean expressionStarted = false;
            StringBuilder expression = null;
            while(index < value.length()) {
                character = value.charAt(index);
                if(character == '{') {
                    if(expressionStarted) {
                        error = Errors.EXPRESSION_OPENSIGN_AFTER_OPEN;
                        return null;
                    }
                    expressionStarted = true;
                    expression = new StringBuilder();
                }else if(character == '}') {
                    if(!expressionStarted) {
                        error = Errors.EXPRESSION_CLOSEDSIGN_WITHOUT_OPEN;
                        return null;
                    }
                    expressionStarted = false;
                    newString.append('1');
                }else {
                    if(!expressionStarted) {
                        newString.append(character);
                    }else {
                        expression.append(character);
                    }
                }
                index++;
            }
            if(expressionStarted) {
                error = Errors.EXPRESSION_NOT_CLOSED;
                return null;
            }
            return newString.toString();
        }

        @Override
        protected Integer onPostTransformValidity() {
            Integer e = this.error;
            this.error = null;
            return e;
        }
    };

    private final FieldValidator nameValidator = FieldValidator.builder(this::getName)
            .rules(FieldRules.REQUIRED())
            .rules(new ExpressionRuleWrapper())
            .build();

    private final FieldValidator userNameValidator = FieldValidator.builder(this::getUserName)
            .rules(new ExpressionRuleWrapper())
            .basicCondition(isModelNullCondition)
            .build();
    private final FieldValidator urlValidator = FieldValidator.builder(this::getUrl)
            .rules(FieldRules.REQUIRED())
            .rules(new ExpressionRuleWrapper())
            .build();
    private final FieldValidator addressIpValidator = FieldValidator.builder(this::getAddressIp)
            .rules(FieldRules.REQUIRED())
            .rules(new ExpressionRuleWrapper(FieldRules.ADDRESS_IP()))
            .basicCondition(isModelNullCondition)
            .build();
    private final FieldValidator portValidator = FieldValidator.builder(ValueProvider.transform(new ExpressionRemoveTransform(), this::getPort))
            .rules(FieldRules.REQUIRED())
            .rules(new ExpressionRuleWrapper(FieldRules.NUMERIC()))
            .basicCondition(isModelNullCondition)
            .build();
    private final FieldValidator channelValidator = FieldValidator.builder(ValueProvider.transform(new ExpressionRemoveTransform(), this::getChannel))
            .rules(FieldRules.REQUIRED())
            .basicCondition(isModelNullCondition)
            .rules(new ExpressionRuleWrapper(FieldRules.NUMERIC()))
            .build();

    @Bindable
    public FieldValidator getNameValidator() {
        return this.nameValidator;
    }

    public FieldValidator getUserNameValidator() {
        return userNameValidator;
    }

    public FieldValidator getAddressIpValidator() {
        return addressIpValidator;
    }

    public FieldValidator getPortValidator() {
        return portValidator;
    }

    public FieldValidator getChannelValidator() {
        return channelValidator;
    }

    public FieldValidator getUrlValidator() {
        return urlValidator;
    }

    private boolean advancedVisible;
    private boolean formVisible;
    private boolean channelInputVisibile;
    private boolean streamTypeInputVisibile;
    private boolean serverUrlInputVisibile;

    private final MutableLiveData<Producer> producer = new MutableLiveData<>();
    private final MutableLiveData<Model> model = new MutableLiveData<>();
    private final MutableLiveData<Integer> streamType = new MutableLiveData<>(UrlTemplate.StreamType.MAIN_STREAM);

    private UrlComponentsProvider fieldProvider = new CameraFormModelUrlComponentProvider(this);

    @Override
    public void notifyPropertyChanged(int propertyId) {
        super.notifyPropertyChanged(propertyId);

        if(propertyId == BR.name) {
            if(name == null) {
                name = "";
            }else {
                nameValidator.isValid(true);
            }
        }else if(propertyId == BR.userName) {
            userNameValidator.isValid(true);
        }else if(propertyId == BR.addressIp) {
            addressIpValidator.isValid(true);
        }else if(propertyId == BR.port) {
            portValidator.isValid(true);
        }else if(propertyId == BR.channel) {
            channelValidator.isValid(true);
        }else if(propertyId == BR.url) {
            urlValidator.isValid(true);
        }

        if(propertyId != BR.url)
            tryUpdateCameraUrl();
    }

    public boolean isValidUrlTemplateForm() {
        return isValidUrlTemplateForm(false);
    }

    public boolean isValidUrlTemplateForm(boolean showError) {
        if(this.model.getValue() == null)
            return true;
        boolean valid = true;
        if(!userNameValidator.isValid(showError))
            valid = false;
        if(!addressIpValidator.isValid(showError))
            valid = false;
        if(!portValidator.isValid(showError))
            valid = false;
        if(channelInputVisibile && !channelValidator.isValid(showError))
            valid = false;
        return valid;
    }

    public boolean isUrlFormValid() {
        return isUrlFormValid(true);
    }

    public boolean isUrlFormValid(boolean showError) {
        boolean valid = true;
        if(!isValidUrlTemplateForm(showError))
            valid = false;
        if(!urlValidator.isValid(showError))
            valid = false;
        return valid;
    }

    public boolean isFormValid() {
        boolean valid = true;
        if(!nameValidator.isValid(true))
            valid = false;
        if(!isUrlFormValid(true))
            valid = false;
        return valid;
    }

    public void tryUpdateCameraUrl() {
        Producer producerSelected = this.producer.getValue();
        if(producerSelected == null)
            return;
        Model modelSelected = this.model.getValue();
        if(modelSelected == null)
            return;
        if(!isValidUrlTemplateForm())
            return;
        setUrlWithoutResetingUrlTemplate(UrlGenerator.generate(modelSelected.getUrlTemplate(), this.fieldProvider));
    }

    private void updateAdditionalFieldsVisibility() {
        if(model.getValue() == null) {
            setFormVisible(false);
            return;
        }
        setFormVisible(true);

        UrlTemplate urlTemplate = model.getValue().getUrlTemplate();
        setChannelInputVisibile(urlTemplate.useField(UrlTemplate.AdditionalFields.Channel));
        setStreamTypeInputVisibile(urlTemplate.useField(UrlTemplate.AdditionalFields.Stream));
        setServerUrlInputVisibile(urlTemplate.useField(UrlTemplate.AdditionalFields.ServerUrl));
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if(Objects.equals(this.name, name))
            return;
        this.name = name;
        notifyPropertyChanged(BR.name);
    }

    @Bindable
    public String getProducerName() {
        return producerName;
    }

    public MutableLiveData<Producer> getProducer() {
        return producer;
    }

    public void setProducer(Producer producer) {
        if(this.producer.getValue() == producer) return;
        if(producer == null) {
            this.addressIpValidator.setError(null);
            this.portValidator.setError(null);
            this.channelValidator.setError(null);
        }
        this.producer.setValue(producer);
        this.producerName = (producer != null)? producer.getName() : null;
        notifyPropertyChanged(BR.producerName);
    }

    public MutableLiveData<Model> getModel() {
        return model;
    }

    @Bindable
    public String getModelName() {
        return modelName;
    }

    public void setModel(Model model) {
        if(this.model.getValue() == model) return;
        this.model.setValue(model);
        this.modelName = (model != null)? model.getName() : null;
        notifyPropertyChanged(BR.modelName);
        updateAdditionalFieldsVisibility();
    }

    @Bindable
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        notifyPropertyChanged(BR.userName);
    }

    @Bindable
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        notifyPropertyChanged(BR.password);
    }

    @Bindable
    public String getAddressIp() {
        return addressIp;
    }

    public void setAddressIp(String addressIp) {
        this.addressIp = addressIp;
        notifyPropertyChanged(BR.addressIp);
    }

    @Bindable
    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
        notifyPropertyChanged(BR.port);
    }

    @Bindable
    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
        notifyPropertyChanged(BR.serverUrl);
    }

    @Bindable
    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
        notifyPropertyChanged(BR.channel);
    }

    public void setStreamType(@UrlTemplate.StreamType int streamType) {
        this.streamType.setValue(streamType);
    }

    @Bindable
    public String getUrl() {
        return url;
    }

    public void setUrlWithoutResetingUrlTemplate(String url) {
        this.url = url;
        notifyPropertyChanged(BR.url);
    }

    public void setUrl(String url) {
        if(url != null && url.equals(this.url) ||
                url == null && this.url == null)
            return;
        setUrlWithoutResetingUrlTemplate(url);
        setProducer(null);
        setModel(null);
    }

    public MutableLiveData<Integer> getStreamType() {
        return streamType;
    }

    @Bindable
    public boolean isAdvancedVisible() {
        return advancedVisible;
    }

    public void setAdvancedVisible(boolean advancedVisible) {
        this.advancedVisible = advancedVisible;
        notifyPropertyChanged(BR.advancedVisible);
    }

    @Bindable
    public boolean isFormVisible() {
        return formVisible;
    }

    public void setFormVisible(boolean formVisible) {
        this.formVisible = formVisible;
        notifyPropertyChanged(BR.formVisible);
    }

    @Bindable
    public boolean isChannelInputVisibile() {
        return channelInputVisibile;
    }

    public void setChannelInputVisibile(boolean channelInputVisibile) {
        this.channelInputVisibile = channelInputVisibile;
        notifyPropertyChanged(BR.channelInputVisibile);
    }

    @Bindable
    public boolean isStreamTypeInputVisibile() {
        return streamTypeInputVisibile;
    }

    public void setStreamTypeInputVisibile(boolean streamTypeInputVisibile) {
        this.streamTypeInputVisibile = streamTypeInputVisibile;
        notifyPropertyChanged(BR.streamTypeInputVisibile);
    }

    @Bindable
    public boolean isServerUrlInputVisibile() {
        return serverUrlInputVisibile;
    }

    public void setServerUrlInputVisibile(boolean serverUrlInputVisibile) {
        this.serverUrlInputVisibile = serverUrlInputVisibile;
        notifyPropertyChanged(BR.serverUrlInputVisibile);
    }

    public CameraGroupModel toModel() {
        CameraGroupModel model = new CameraGroupModel();
        model.setName(name);
        model.setProducer(this.producer.getValue());
        model.setModel(this.model.getValue());
        model.setUserName(userName);
        model.setPassword(password);
        model.setAddressIp(addressIp);
        model.setPort(port);
        model.setServerUrl(serverUrl);
        model.setChannel(channel);
        model.setStreamType(Objects.requireNonNullElse(streamType.getValue(), UrlTemplate.StreamType.MAIN_STREAM));
        model.setUrl(url);
        return model;
    }

    public void fromModel(CameraPattern cameraPattern) {
        setName(cameraPattern.getName());
        setUserName(cameraPattern.getUserName());
        setPassword(cameraPattern.getPassword());
        setAddressIp(cameraPattern.getAddressIp());
        setPort(cameraPattern.getPort());
        setServerUrl(cameraPattern.getServerUrl());
        setChannel(Strings.isNullOrEmpty(cameraPattern.getChannel())? "0" : cameraPattern.getChannel());
        try {
            setStreamType(Objects.requireNonNullElse((cameraPattern.getStream() == null) ? null : Integer.parseInt(cameraPattern.getStream()), UrlTemplate.StreamType.MAIN_STREAM));
        }catch (NumberFormatException e) {
            setStreamType(UrlTemplate.StreamType.MAIN_STREAM);
        }
        setUrlWithoutResetingUrlTemplate(cameraPattern.getUrl());
    }

    public void clearNameWithoutErrorMessage() {
        setName(null);
    }

    public static class CameraFormModelUrlComponentProvider implements UrlComponentsProvider {

        private final CameraFormModel cameraFormModel;
        public CameraFormModelUrlComponentProvider(CameraFormModel cameraFormModel){
            this.cameraFormModel = cameraFormModel;
        }
        @Override
        public boolean usesAuthorization() {
            if(cameraFormModel.userName == null)
                return false;
            return cameraFormModel.userName.replaceAll(" ", "").length() > 0;
        }

        @Override
        public String getUserName() {
            if(cameraFormModel.userName == null)
                return "";
            return cameraFormModel.userName.replaceAll(" ", "");
        }

        @Override
        public String getPassword() {
            return Objects.requireNonNullElse(cameraFormModel.password, "");
        }

        @Override
        public String getAddressIp() {
            return cameraFormModel.addressIp;
        }

        @Override
        public String getPort() {
            return cameraFormModel.port;
        }

        @Override
        public String getChannel() {
            return cameraFormModel.channel;
        }

        @Override
        public String getServerUrl() {
            return Objects.requireNonNullElse(cameraFormModel.serverUrl, "");
        }

        @Override
        public @UrlTemplate.StreamType int getStream() {
            return cameraFormModel.streamType.getValue();
        }
    }
}
