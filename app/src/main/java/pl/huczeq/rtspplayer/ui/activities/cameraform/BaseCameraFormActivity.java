package pl.huczeq.rtspplayer.ui.activities.cameraform;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.Observer;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.adapters.ModelsSpinnerAdapter;
import pl.huczeq.rtspplayer.adapters.ProducersSpinnerAdapter;
import pl.huczeq.rtspplayer.data.CameraInstancesFactory;
import pl.huczeq.rtspplayer.data.objects.CameraInstance;
import pl.huczeq.rtspplayer.data.objects.CameraPattern;
import pl.huczeq.rtspplayer.data.expression.Variable;
import pl.huczeq.rtspplayer.data.repositories.UrlTemplatesRepository;
import pl.huczeq.rtspplayer.exceptions.ParsingException;
import pl.huczeq.rtspplayer.interfaces.IOnDataUpdated;
import pl.huczeq.rtspplayer.interfaces.RightDrawableOnTouchListener;
import pl.huczeq.rtspplayer.ui.activities.base.BaseActivity;
import pl.huczeq.rtspplayer.ui.activities.camerapreviews.BasePreviewCameraActivity;
import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.data.objects.urltemplates.Model;
import pl.huczeq.rtspplayer.data.objects.urltemplates.Producer;
import pl.huczeq.rtspplayer.data.objects.urltemplates.UrlTemplate;
import pl.huczeq.rtspplayer.data.utils.DataState;

public class BaseCameraFormActivity extends BaseActivity implements UrlTemplatesRepository.FormCallback, IOnDataUpdated {

    private final String TAG = "BaseCameraActivity";

    public static String EXTRA_CAMERA_ID = "cameraId";
    public static String EXTRA_DATA_FROM_CAMERA_INSTANCE = "dataFromCamera";

    boolean doubleBackToExitPressedOnce = false;
    int doubleBackToExitDelay = 1500;

    protected EditText etCameraName, etCameraUrl, etUserName, etPassword, etIp, etPort, etChannel, etServerUrl;
    protected TextView tvProducers, tvModels;
    protected Spinner spinStreamType, spinProducers, spinModel;
    protected LinearLayout llMoreForm;
    protected ProgressBar pbLoadingProducers;

    protected Button buttonShowMore;
    protected FloatingActionButton buttonAddCamera;
    protected FloatingActionButton buttonStartCameraPreview;

    protected ImageButton buttonAddVariable;
    protected LinearLayout llVariables;

    protected UrlTemplate urlTemplate;
    protected String previousUrl;

    protected ProducersSpinnerAdapter producersAdapter;
    protected ModelsSpinnerAdapter modelsAdapter;
    protected LinkedHashMap<View, Variable> variables;

    protected CameraPattern cameraPatternToLoad;

    FABProgressCircle saveCameraFAButtonCircle;

    TextWatcher textWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            updateUrl();
        }
        @Override public void afterTextChanged(Editable editable) { }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_form);

        this.producersAdapter = new ProducersSpinnerAdapter(getBaseContext(), new ArrayList<Producer>());
        this.modelsAdapter = new ModelsSpinnerAdapter(getBaseContext(), new ArrayList<Model>());
        this.variables = new LinkedHashMap<>();

        DataManager.getInstance(getApplicationContext()).getUrlTemplatesRepositoryState().observe(this, new Observer<DataState>() {
            @Override
            public void onChanged(DataState state) {
                Log.d(TAG, state.toString());
                if(state == DataState.LOADING) {
                    pbLoadingProducers.setVisibility(View.VISIBLE);
                }else if(state == DataState.LOADED){
                    onDataLoaded();
                }else {
                    DataManager.getInstance(getApplicationContext()).loadUrlTemplates();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        producersAdapter.clear();
        modelsAdapter.clear();
    }

    @Override
    public void onBackPressed() {
        if(this.doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getResources().getString(R.string.double_back_2_exit_info), Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                BaseCameraFormActivity.this.doubleBackToExitPressedOnce = false;
            }
        }, this.doubleBackToExitDelay);
    }

    @Override
    protected void setViewsWidgets() {
        super.setViewsWidgets();

        etCameraName = findViewById(R.id.etCameraName);
        etCameraUrl = findViewById(R.id.etCameraUrl);
        etUserName = findViewById(R.id.etUserName);
        etPassword = findViewById(R.id.etPassword);
        etIp = findViewById(R.id.etIp);
        etPort = findViewById(R.id.etPort);
        etChannel = findViewById(R.id.etChannel);
        etServerUrl = findViewById(R.id.etServerUrl);
        tvProducers = findViewById(R.id.tvProducers);
        tvModels = findViewById(R.id.tvModels);
        spinStreamType = findViewById(R.id.spinnerStreamType);
        spinProducers = findViewById(R.id.spinnerProducers);
        spinModel = findViewById(R.id.spinnerModel);
        llMoreForm = findViewById(R.id.llMoreForm);
        buttonShowMore = findViewById(R.id.buttonShowMore);
        pbLoadingProducers = findViewById(R.id.pbLoadingProducers);
        buttonAddVariable = findViewById(R.id.buttonAddVariable);
        llVariables = findViewById(R.id.lvVariables);
        buttonAddCamera = findViewById(R.id.saveCameraFAButton);
        buttonStartCameraPreview = findViewById(R.id.startPreviewFAButton);
        saveCameraFAButtonCircle = findViewById(R.id.saveCameraFAButtonCircle);

        etCameraName.setOnTouchListener(new RightDrawableOnTouchListener(etCameraName) {
            @Override
            public boolean onDrawableTouch(MotionEvent event) {
                etCameraName.setText("");
                return false;
            }
        });
        etPassword.setOnTouchListener(new RightDrawableOnTouchListener(etPassword) {
            @Override
            public boolean onDrawableTouch(MotionEvent event) {
                if(etPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                    etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_hide, 0);
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
                }else {
                    etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_show, 0);
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    etPassword.setTypeface(Typeface.DEFAULT);
                }
                return true;
            }
        });

        producersAdapter.setProducersList(new ArrayList<Producer>(), null);
        modelsAdapter.setModelsList(new ArrayList<Model>(), null);
        spinProducers.setAdapter(producersAdapter);
        spinModel.setAdapter(modelsAdapter);

        etCameraUrl.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b) {
                    previousUrl = etCameraUrl.getText().toString();
                }else {
                    previousUrl = null;
                }
            }
        });
        etCameraUrl.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                Log.d(TAG, "afterTextChanged");
                if(urlTemplate != null && previousUrl != null) {
                    if(!previousUrl.equalsIgnoreCase(etCameraUrl.getText().toString())) {
                        spinProducers.setSelection(0, false);
                        onProducerSelected(producersAdapter.getItem(0));
                    }
                }
            }
        });

        etUserName.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);
        etIp.addTextChangedListener(textWatcher);
        etChannel.addTextChangedListener(textWatcher);
        etServerUrl.addTextChangedListener(textWatcher);
        etPort.addTextChangedListener(textWatcher);

        spinStreamType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateUrl();
            }
            @Override public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        buttonShowMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(llMoreForm.getVisibility() == View.VISIBLE) {
                    llMoreForm.setVisibility(View.GONE);
                    buttonShowMore.setText(R.string.label_show_more);
                }else {
                    llMoreForm.setVisibility(View.VISIBLE);
                    buttonShowMore.setText(R.string.label_show_less);
                }
            }
        });
        buttonAddVariable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewVariable(null);
            }
        });
        buttonAddCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickButtonSaveCamera();
            }
        });

        buttonStartCameraPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickButtonStartCameraPreview();
            }
        });
    }

    public int getProducerIndex(String name) {
        Log.d(TAG, "getProducerIndex(" + name + ")");
        for(int i = 0; i<this.dataManager.getUrlTemplates().size(); i++) {
            if(this.dataManager.getUrlTemplates().get(i).getName().equals(name)) return i;
        }
        Log.d(TAG, "getProducerIndex - not found producer");
        return -1;
    }

    public void onDataLoaded() {
        Log.d(TAG, "onDataLoaded()");
        pbLoadingProducers.setVisibility(View.GONE);
        int producerIndex = -1, modelIndex = 0;
        Log.d(TAG, (this.cameraPatternToLoad == null)? "CameraPatternToLoad is null!" : "CameraPatternToLoad is not null");
        if(this.cameraPatternToLoad != null) {
            if(this.cameraPatternToLoad.getProducer() != null && !ProducersSpinnerAdapter.emptyProducer.getName().equalsIgnoreCase(this.cameraPatternToLoad.getProducer()) && !this.cameraPatternToLoad.getProducer().trim().isEmpty()) {
                producerIndex = getProducerIndex(this.cameraPatternToLoad.getProducer());
                Log.d(TAG, "Load: " + this.cameraPatternToLoad.getProducer() + " , index: " + producerIndex);
                if (producerIndex > -1) {
                    Producer producer = dataManager.getUrlTemplates().get(producerIndex);
                    if(this.cameraPatternToLoad.getModel() != null && !this.cameraPatternToLoad.getModel().trim().isEmpty()) {
                        modelIndex = producer.getModelIndex(this.cameraPatternToLoad.getModel());
                        Log.d(TAG, "Model loading(model = " + this.cameraPatternToLoad.getModel() + ":index = " + Integer.toString(modelIndex) + ")");
                        if (modelIndex == -1) {
                            modelIndex = 0;
                        }
                    }
                }
            }
        }

        Log.d(TAG, "producerIndex: " + producerIndex);
        Log.d(TAG, "modelIndex: " + modelIndex);

        producersAdapter.setProducersList(dataManager.getUrlTemplates(), null);

        Log.d(TAG, "onDataLoaded.Producer: " + producersAdapter.getItem(producerIndex+1).getName());

        if(this.cameraPatternToLoad != null && producerIndex != -1)
            modelsAdapter.setModelsList(dataManager.getUrlTemplates().get(producerIndex).getModelList(), null);
        else modelsAdapter.setModelsList(new ArrayList<Model>(), null);

        spinProducers.setSelection(-1, true);
        if(this.cameraPatternToLoad != null) {
            spinProducers.setSelection(producerIndex + 1, true);
        }else {
            spinProducers.setSelection(0, true);
        }
        spinModel.setSelection(modelIndex, true);
        /*
        spinProducers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public boolean firstInit = true;
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(firstInit)
                    onProducerSelected(producersAdapter.getItem(i));
                else
                    firstInit = true;
                Log.d(TAG, "spinProducers.OnItemSelectedListener");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        */
        spinProducers.setOnItemSelectedListener(new IgnoreInitOnItemSelectedListener(){
            @Override
            void onItemSelectedAfterInit(AdapterView<?> adapterView, View view, int i, long l) {
                onProducerSelected(BaseCameraFormActivity.this.producersAdapter.getItem(i));
            }
        });
        spinModel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public boolean firstInit = true;
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "spinModel.OnItemSelectedListener");
                if(firstInit)
                    onModelSelected(modelsAdapter.getItem(i));
                else
                    firstInit = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        pbLoadingProducers.setVisibility(View.GONE);
        spinProducers.setVisibility(View.VISIBLE);
        tvProducers.setVisibility(View.VISIBLE);
        if(modelsAdapter.getCount() > 1) {
            spinModel.setVisibility(View.VISIBLE);
            tvModels.setVisibility(View.VISIBLE);
        }
        if(modelsAdapter.getCount() > 0 && modelIndex < modelsAdapter.getCount()) {
            if (modelsAdapter.getItem(modelIndex) == null) {
                urlTemplate = null;
            } else {
                urlTemplate = modelsAdapter.getItem(modelIndex).getUrlTemplate();
                updateUrl();
            }
            Log.d(TAG, "onDataLoaded.Model: " + modelsAdapter.getItem(modelIndex).getName());
        }
        updateVisibility();
    }
    public void onProducerSelected(Producer producer) {
        loadModels(producer);
    }

    private void loadModels(Producer producer) {
        if(producer == ProducersSpinnerAdapter.emptyProducer) {
            modelsAdapter.setModelsList(new ArrayList<Model>(), spinModel);
            spinModel.setSelection(-1, false);

            spinModel.setVisibility(View.GONE);
            tvModels.setVisibility(View.GONE);
            onModelSelected(null);
            return;
        }
        modelsAdapter.setModelsList(producer.getModelList(), spinProducers);
        spinModel.setSelection(0);
        onModelSelected(modelsAdapter.getItem(0));

        if(producer.getModelList().size() > 1) {
            spinModel.setVisibility(View.VISIBLE);
            tvModels.setVisibility(View.VISIBLE);
        }else {
            spinModel.setVisibility(View.GONE);
            tvModels.setVisibility(View.GONE);
        }
    }

    public void onModelSelected(Model model) {
        if(model == null) {
            urlTemplate = null;
        }else {
            urlTemplate = model.getUrlTemplate();
            updateUrl();
        }
        updateVisibility();
    }

    private void updateVisibility() {
        if(urlTemplate == null) {
            setVisiblity(etUserName, false);
            setVisiblity(etPassword, false);
            setVisiblity(etIp, false);
            setVisiblity(etPort, false);
            setVisiblity(etChannel, false);
            setVisiblity(spinStreamType, false);
            setVisiblity(etServerUrl, false);
        }else {
            setVisiblity(etUserName, true);
            setVisiblity(etPassword, true);
            setVisiblity(etIp, true);
            setVisiblity(etPort, true);
            setVisiblity(etChannel, urlTemplate.useField(UrlTemplatesRepository.AdditionalField.Channel));
            setVisiblity(spinStreamType, urlTemplate.useField(UrlTemplatesRepository.AdditionalField.Stream));
            setVisiblity(etServerUrl, urlTemplate.useField(UrlTemplatesRepository.AdditionalField.ServerUrl));
        }
    }
    protected void setVisiblity(View view, boolean visible) {
        if(visible) {
            view.setVisibility(View.VISIBLE);
        }else {
            view.setVisibility(View.GONE);
        }
    }

    protected CameraPattern getCamera() {
        CameraPattern newCameraPattern = new CameraPattern();
        newCameraPattern.setName(etCameraName.getText().toString());
        newCameraPattern.setUrl(etCameraUrl.getText().toString());

        if(this.dataManager.getUrlTemplatesRepositoryState().getValue() == DataState.LOADED) {
            Producer producer = null;
            if (spinProducers.getSelectedItemPosition() > 0 && spinProducers.getSelectedItemPosition() < producersAdapter.getCount()) {
                producer = producersAdapter.getItem(spinProducers.getSelectedItemPosition());
            }
            if (producer == null) {
                newCameraPattern.setProducer("");
            } else {
                newCameraPattern.setProducer(producer.getName());
            }
            Log.d(TAG, "getCamera():producer = " + newCameraPattern.getProducer());

            Model model = null;
            Log.d(TAG, "getCamera():spinModel.getSelectedItemPosition() = " + Integer.toString(spinModel.getSelectedItemPosition()));
            if (producer != null && spinModel.getSelectedItemPosition() >= 0 && spinModel.getSelectedItemPosition() < modelsAdapter.getCount()) {
                model = modelsAdapter.getItem(spinModel.getSelectedItemPosition());
            }
            if (model == null) {
                newCameraPattern.setModel("");
            } else {
                newCameraPattern.setModel(model.getName());
            }
        }else {
            newCameraPattern.setProducer(this.cameraPatternToLoad.getProducer());
            newCameraPattern.setModel(this.cameraPatternToLoad.getModel());
        }

        newCameraPattern.setUserName(etUserName.getText().toString());
        newCameraPattern.setPassword(etPassword.getText().toString());
        newCameraPattern.setAddressIp(etIp.getText().toString());
        newCameraPattern.setPort(etPort.getText().toString());
        newCameraPattern.setChannel(etChannel.getText().toString());
        newCameraPattern.setStream(String.valueOf(spinStreamType.getSelectedItemPosition()));
        newCameraPattern.setServerUrl(etServerUrl.getText().toString());
        Collection<Variable> variables = this.variables.values();
        newCameraPattern.setVariables(variables);

        return newCameraPattern;
    }

    protected String isUrlFormCorrectGetReason() {
        Log.d(TAG, "Domain: " + Patterns.DOMAIN_NAME.matcher(etIp.getText().toString()).matches());
        Log.d(TAG, "Ip: " + Patterns.IP_ADDRESS.matcher(etIp.getText().toString()).matches());
        //if(etIp.getText().toString().length() == 0 || (!Patterns.DOMAIN_NAME.matcher(etIp.getText().toString()).matches() && !Patterns.IP_ADDRESS.matcher(etIp.getText().toString()).matches())) {
        /*if(etIp.getText().toString().length() == 0 || (!etIp.getText().toString().matches(SpecialExpression.variableRegex+"\\."+ SpecialExpression.variableRegex+"\\."+ SpecialExpression.variableRegex+"\\."+ SpecialExpression.variableRegex) && !etIp.getText().toString().matches("[0-9]+"+"\\."+"[0-9]+"+"\\."+"[0-9]+"+"\\."+"[0-9]+"))) {
            return getResources().getString(R.string.incorrect_addressip);
        }
        if(etPort.getText().toString().length() == 0 || !etPort.getText().toString().matches(SpecialExpression.variableRegex)) {
            return getResources().getString(R.string.incorrect_port);
        }
        if(etChannel.getVisibility()==View.VISIBLE && (etChannel.getText().toString().length() == 0 || !etChannel.getText().toString().matches(SpecialExpression.variableRegex))) {
            return getResources().getString(R.string.incorrect_channel);
        }*/
        return "";
    }
    protected boolean isUrlFormCorrect(boolean showMessage) {
        if(this.urlTemplate == null){
            if(etCameraUrl.getText() == null || etCameraUrl.getText().toString().trim().isEmpty()) {
                if(showMessage) {
                    showMessageError(getResources().getString(R.string.incorrect_camera_url));
                }
                return false;
            }
            return true;
        }
        String reason = isUrlFormCorrectGetReason();
        Log.d(TAG, "Reason: " + reason);
        if(!reason.equals("")) {
            if(showMessage) showMessageError(reason);
            return false;
        }
        return true;
    }
    protected boolean isUrlFormCorrect() {
        return isUrlFormCorrect(false);
    }

    protected String isCameraFormCorrectGetReason() {
        if(etCameraName.getText().length() == 0) {
            return getResources().getString(R.string.incorrect_camera_name);
        }
        if(etCameraUrl.getText().length() == 0) {
            return getResources().getString(R.string.incorrect_camera_url);
        }
        int i = 0;
        Log.d(TAG, "Variables list: " + this.variables.values().size());
        List<String> varNames = new ArrayList<>();
        for(Variable variable : this.variables.values()) {
            i++;
            if(variable.getName().equals("")) {
                return "Name empty: " + i;
            }
            if(varNames.contains(variable.getName())) {
                return "Duplicate name: " + variable.getName();
            }
            varNames.add(variable.getName());
            try {
                variable.parse();
            } catch (ParsingException e) {
                e.printStackTrace();
                return "Invalid variable (" + i + "): " + variable.getName();
            }
        }
        return "";
    }
    protected boolean isCameraFormCorrect(boolean showMessage) {
        String reason = isCameraFormCorrectGetReason();
        if(!reason.equals("")) {
            if(showMessage) showMessageError(reason);
            return false;
        }
        return true;
    }
    protected boolean isCameraFormCorrect() {
        return isCameraFormCorrect(false);
    }

    protected boolean isFormCorrect(boolean showMessage) {
        Log.d(TAG, String.valueOf(showMessage));
        if(isUrlFormCorrect(showMessage)) {
            Log.d(TAG, "url correct");
            if(isCameraFormCorrect(showMessage)) {
                Log.d(TAG, "camera correct");
                return true;
            }
        }
        return false;
    }

    protected void showMessageError(String message) {
        Log.d(TAG, message);
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    protected void onClickButtonSaveCamera() {}
    protected void onClickButtonStartCameraPreview() {
        /*if(!isUrlFormCorrect()) {
            Toast.makeText(this, getResources().getString(R.string.incorrect_camera_url), Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(BasePreviewCameraActivity.getPreviewCameraIntent(getApplicationContext(), etCameraUrl.getText().toString()));*/
        if(!isUrlFormCorrect()) {
            Toast.makeText(this, getResources().getString(R.string.incorrect_camera_url), Toast.LENGTH_SHORT).show();
            return;
        }
        String previewUrl;
        Log.d(TAG, "Size: " + this.variables.size());
        if(this.variables.size() > 0) {
            List<CameraInstance> cameraInstance = new CameraInstancesFactory(getCamera()).setOnlyOneInsance(true).build();
            if(cameraInstance.size() <= 0)
                return;
            previewUrl = cameraInstance.get(0).getUrl();
        }else {
            previewUrl = this.etCameraUrl.getText().toString();
        }
        startActivity(BasePreviewCameraActivity.getStartIntent(getApplicationContext(), previewUrl));
    }

    public void updateUrl() {
        if(urlTemplate != null && isUrlFormCorrect() && previousUrl==null) {
                etCameraUrl.setText(urlTemplate.getFullUrl(this));
        }
    }

    public void loadCameraToForm() {
        buttonAddCamera.setEnabled(true);
        etCameraName.setText(this.cameraPatternToLoad.getName());
        etUserName.setText(this.cameraPatternToLoad.getUserName());
        etPassword.setText(this.cameraPatternToLoad.getPassword());
        etIp.setText(this.cameraPatternToLoad.getAddressIp());
        etPort.setText(this.cameraPatternToLoad.getPort());
        etChannel.setText(this.cameraPatternToLoad.getChannel());
        try {
            spinStreamType.setSelection(Integer.parseInt(this.cameraPatternToLoad.getStream()));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            spinStreamType.setSelection(0);
        }
        /*
        Log.d(TAG, String.valueOf(dataManager.isUrlsTemplatesLoaded()));
        if(dataManager.isUrlsTemplatesLoaded()) this.onDataLoaded();*/

        if(llMoreForm.getVisibility() != View.VISIBLE && this.cameraPatternToLoad.getProducer() != null && this.cameraPatternToLoad.getProducer().length() > 0) {
            buttonShowMore.callOnClick();
        }
        etServerUrl.setText(this.cameraPatternToLoad.getServerUrl());

        etCameraUrl.setText(this.cameraPatternToLoad.getUrl());

        this.llVariables.removeAllViews();
        this.variables.clear();
        HashMap<String, String> vars = this.cameraPatternToLoad.getVariables();
        if(vars != null) {
            for (Map.Entry<String, String> var : vars.entrySet()) {
                addNewVariable(new Variable(var.getKey(), var.getValue()));
            }
        }


        if(this.pbLoadingProducers.getVisibility() != View.VISIBLE) {
            int producerIndex = -1, modelIndex = 0;
            Log.d(TAG, (this.cameraPatternToLoad == null) ? "CameraPatternToLoad is null!" : "CameraPatternToLoad is not null");
            if (this.cameraPatternToLoad != null) {
                if (this.cameraPatternToLoad.getProducer() != null && !ProducersSpinnerAdapter.emptyProducer.getName().equalsIgnoreCase(this.cameraPatternToLoad.getProducer()) && !this.cameraPatternToLoad.getProducer().trim().isEmpty()) {
                    producerIndex = getProducerIndex(this.cameraPatternToLoad.getProducer());
                    Log.d(TAG, "Load: " + this.cameraPatternToLoad.getProducer() + " , index: " + producerIndex);
                    if (producerIndex > -1) {
                        Producer producer = dataManager.getUrlTemplates().get(producerIndex);
                        if (this.cameraPatternToLoad.getModel() != null && !this.cameraPatternToLoad.getModel().trim().isEmpty()) {
                            modelIndex = producer.getModelIndex(this.cameraPatternToLoad.getModel());
                            Log.d(TAG, "Model loading(model = " + this.cameraPatternToLoad.getModel() + ":index = " + Integer.toString(modelIndex) + ")");
                            if (modelIndex == -1) {
                                modelIndex = 0;
                            }
                        }
                    }
                }
            }
            Log.d(TAG, "Test: " + spinProducers.getSelectedItemPosition() + " ? " + (producerIndex+1));
            //TODO CHANGED 04.08 - nie otwiera sie formularz czasami
            //Chyba naprawione???????
            if(spinProducers.getSelectedItemPosition() != producerIndex+1) {
                if(this.spinProducers.getOnItemSelectedListener().getClass().isAssignableFrom(IgnoreInitOnItemSelectedListener.class)) {
                    ((IgnoreInitOnItemSelectedListener) this.spinProducers.getOnItemSelectedListener()).firstInit = false;
                }
                spinProducers.setSelection(producerIndex + 1, false);
                loadModels(producersAdapter.getItem(producerIndex + 1));
                spinModel.setSelection(modelIndex, false);
            }
        }
    }

    protected void addNewVariable(Variable var) {
        if(var == null) {
            var = new Variable("", "");
        }
        if(this.variables.size() >= 10) {
            Toast.makeText(this, getString(R.string.error_too_many_variables), Toast.LENGTH_SHORT).show();
            return;
        }
        Variable variable = var;
        LayoutInflater inflater = (LayoutInflater) BaseCameraFormActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View view = inflater.inflate(R.layout.item_variable_view, llVariables, false);

        final EditText etVariableName = view.findViewById(R.id.etVariableName);
        final EditText etVariableValue = view.findViewById(R.id.etVariableValue);
        final ImageButton buttonDeleteVariable = view.findViewById(R.id.buttonDeleteVariable);

        etVariableName.setText(variable.getName());
        etVariableValue.setText(variable.getValue());

        etVariableName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                variable.setName(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        etVariableValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                variable.setValue(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        buttonDeleteVariable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                variables.remove(view);
                llVariables.removeView(view);
            }
        });

        llVariables.addView(view);
        variables.put(view, variable);
    }

    @Override
    public boolean isEmptyAuth() {
        return etUserName.getText().length()==0;
    }

    @Override
    public String getUser() {
        return etUserName.getText().toString();
    }

    @Override
    public String getPassword() {
        return etPassword.getText().toString();
    }

    @Override
    public String getAddressIp() {
        return etIp.getText().toString();
    }

    @Override
    public String getPort() {
        return etPort.getText().toString();
    }

    @Override
    public String getChannel() {
        return etChannel.getText().toString();
    }

    @Override
    public String getServerUrl() {
        String serverUrl = etServerUrl.getText().toString();
        if(!serverUrl.startsWith("/") && !serverUrl.startsWith("\\")) serverUrl = "/" + serverUrl;
        return serverUrl;
    }

    @Override
    public int getStream() {
        return spinStreamType.getSelectedItemPosition();
    }

    protected void onStartDataUpdate() {
        saveCameraFAButtonCircle.show();
        buttonAddCamera.setClickable(false);
    }

    @Override
    public void onComplete() {
        finish();
    }

    protected static abstract class IgnoreInitOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public boolean firstInit = true;

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if(firstInit)
                this.onItemSelectedAfterInit(adapterView, view, i, l);
            else
                firstInit = true;
        }

        abstract void onItemSelectedAfterInit(AdapterView<?> adapterView, View view, int i, long l);

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }
}
