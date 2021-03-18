package pl.huczeq.rtspplayer.ui.activities.base;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.ui.activities.PreviewCameraActivity;
import pl.huczeq.rtspplayer.adapters.ModelsSpinnerAdapter;
import pl.huczeq.rtspplayer.adapters.ProducersSpinnerAdapter;
import pl.huczeq.rtspplayer.interfaces.RightDrawableOnTouchListener;
import pl.huczeq.rtspplayer.ui.views.OnItemSelectedListener;
import pl.huczeq.rtspplayer.utils.Utils;
import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.data.objects.Camera;
import pl.huczeq.rtspplayer.data.UrlsTemplates;
import pl.huczeq.rtspplayer.data.objects.urls.Model;
import pl.huczeq.rtspplayer.data.objects.urls.Producer;
import pl.huczeq.rtspplayer.data.objects.urls.UrlTemplate;

public class BaseCameraActivity extends BaseActivity implements UrlsTemplates.Callback, DataManager.Callback {

    private final String TAG = "BaseCameraActivity";

    public static String EXTRA_CAMERA_NAME = "camera_name";

    boolean doubleBackToExitPressedOnce = false;
    int doubleBackToExitDelay = 1500;

    protected EditText etCameraName, etCameraUrl, etUserName, etPassword, etIp, etPort, etChannel, etServerUrl;
    protected TextView tvProducers, tvModels;
    protected Spinner spinStreamType, spinProducers, spinModel;
    protected LinearLayout llMoreForm;
    protected ProgressBar pbLoadingProducers;

    protected ProducersSpinnerAdapter producersAdapter;
    protected ModelsSpinnerAdapter modelsAdapter;

    protected Button buttonShowMore;
    protected FloatingActionButton buttonAddCamera;
    protected FloatingActionButton buttonStartCameraPreview;

    protected UrlTemplate urlTemplate;
    protected String previousUrl;

    protected Camera cameraToLoad;

    TextWatcher textWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            updateUrl();
        }
        @Override public void afterTextChanged(Editable editable) { }
    };

    OnItemSelectedListener producersOnItemSelected = new OnItemSelectedListener() {
        public void onSelectedItem(AdapterView<?> adapterView, View view, int i, long l) {
            Log.d(TAG, "producersOnItemSelected" + i);
            onProducerSelected(producersAdapter.getItem(i));
        }
    };

    OnItemSelectedListener modelsOnItemSelected = new OnItemSelectedListener() {
        public void onSelectedItem(AdapterView<?> adapterView, View view, int i, long l) {
            Log.d(TAG, "modelsOnItemSelected" + i);
            onModelSelected(modelsAdapter.getItem(i));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addedit_camera);

        producersAdapter = new ProducersSpinnerAdapter(getBaseContext(), new ArrayList<Producer>());
        modelsAdapter = new ModelsSpinnerAdapter(getBaseContext(), new ArrayList<Model>());
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
                BaseCameraActivity.this.doubleBackToExitPressedOnce = false;
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
        buttonAddCamera = findViewById(R.id.saveCameraFAButton);
        buttonStartCameraPreview = findViewById(R.id.startPreviewFAButton);

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

        ////TODO REMOVE?
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

        if(!dataManager.isUrlsTemplatesLoaded()) {
            pbLoadingProducers.setVisibility(View.VISIBLE);
            dataManager.loadUrlsTemplates(this);
        }else {
            this.onDataLoaded();
        }
    }

    @Override
    public void onDataLoaded() {
        pbLoadingProducers.setVisibility(View.GONE);
        int producerIndex = -1, modelIndex = 0;

        if(this.cameraToLoad != null) {
            if(this.cameraToLoad.getProducer() != null && !ProducersSpinnerAdapter.emptyProducer.getName().equalsIgnoreCase(this.cameraToLoad.getProducer()) && !this.cameraToLoad.getProducer().trim().isEmpty()) {
                producerIndex = dataManager.getUrlTemplates().getProducerIndex(this.cameraToLoad.getProducer());
                Log.d(TAG, "Load: " + this.cameraToLoad.getProducer() + " , index: " + producerIndex);
                if (producerIndex > -1) {
                    Producer producer = dataManager.getProducersList().get(producerIndex);
                    if(this.cameraToLoad.getModel() != null && !this.cameraToLoad.getModel().trim().isEmpty()) {
                        modelIndex = producer.getModelIndex(this.cameraToLoad.getModel());
                        Log.d(TAG, "Model loading(model = " + this.cameraToLoad.getModel() + ":index = " + Integer.toString(modelIndex) + ")");
                        if (modelIndex == -1) {
                            modelIndex = 0;
                        }
                    }
                }
            }
        }

        Log.d(TAG, "producerIndex: " + producerIndex);
        Log.d(TAG, "modelIndex: " + modelIndex);

        producersAdapter.setProducersList(dataManager.getProducersList(), null);

        Log.d(TAG, "onDataLoaded.Producer: " + producersAdapter.getItem(producerIndex+1).getName());

        if(this.cameraToLoad != null && producerIndex != -1)
            modelsAdapter.setModelsList(dataManager.getProducersList().get(producerIndex).getModelList(), null);
        else modelsAdapter.setModelsList(new ArrayList<Model>(), null);

        spinProducers.setSelection(-1, true);
        if(this.cameraToLoad != null) {
            spinProducers.setSelection(producerIndex + 1, true);
        }else {
            spinProducers.setSelection(0, true);
        }
        spinModel.setSelection(modelIndex, true);

        spinProducers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean firstInit = true;
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
        spinModel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean firstInit = true;
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
        if(modelsAdapter.getCount() > 1) {
            spinModel.setVisibility(View.VISIBLE);
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
            setVisiblity(etChannel, urlTemplate.useField(UrlsTemplates.AdditionalField.Channel));
            setVisiblity(spinStreamType, urlTemplate.useField(UrlsTemplates.AdditionalField.Stream));
            setVisiblity(etServerUrl, urlTemplate.useField(UrlsTemplates.AdditionalField.ServerUrl));
        }
    }
    protected void setVisiblity(View view, boolean visible) {
        if(visible) {
            view.setVisibility(View.VISIBLE);
        }else {
            view.setVisibility(View.GONE);
        }
    }

    protected Camera getCamera() {
        Camera cam = new Camera(etCameraName.getText().toString());
        cam.setUrl(etCameraUrl.getText().toString());

        Producer producer = null;
        if(spinProducers.getSelectedItemPosition() > 0 && spinProducers.getSelectedItemPosition() < producersAdapter.getCount()) {
            producer = producersAdapter.getItem(spinProducers.getSelectedItemPosition());
        }
        if(producer == null) {
            cam.setProducer("");
        }else {
            cam.setProducer(producer.getName());
        }
        Log.d(TAG, "getCamera():producer = " + cam.getProducer());

        Model model = null;
        Log.d(TAG, "getCamera():spinModel.getSelectedItemPosition() = " + Integer.toString(spinModel.getSelectedItemPosition()));
        if(producer != null && spinModel.getSelectedItemPosition() >= 0 && spinModel.getSelectedItemPosition() < modelsAdapter.getCount()) {
            model = modelsAdapter.getItem(spinModel.getSelectedItemPosition());
        }
        if(model == null) {
            cam.setModel("");
        }else {
            cam.setModel(model.getName());
        }

        cam.setUserName(etUserName.getText().toString());
        cam.setPassword(etPassword.getText().toString());
        cam.setAddressIp(etIp.getText().toString());
        cam.setPort(etPort.getText().toString());
        cam.setChannel(etChannel.getText().toString());
        cam.setStream(String.valueOf(spinStreamType.getSelectedItemPosition()));
        cam.setServerUrl(etServerUrl.getText().toString());
        return cam;
    }

    protected String isUrlFormCorrectGetReason() {
        Log.d(TAG, "Domain: " + Patterns.DOMAIN_NAME.matcher(etIp.getText().toString()).matches());
        Log.d(TAG, "Ip: " + Patterns.IP_ADDRESS.matcher(etIp.getText().toString()).matches());
        if(etIp.getText().toString().length() == 0 || (!Patterns.DOMAIN_NAME.matcher(etIp.getText().toString()).matches() && !Patterns.IP_ADDRESS.matcher(etIp.getText().toString()).matches())) {
            return getResources().getString(R.string.incorrect_addressip);
        }
        if(etPort.getText().toString().length() == 0) {
            return getResources().getString(R.string.incorrect_port);
        }
        if(etChannel.getVisibility()==View.VISIBLE && (etChannel.getText().toString().length() == 0 || !Utils.isNumeric(etChannel.getText().toString()))) {
            return getResources().getString(R.string.incorrect_channel);
        }
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
        if(!isUrlFormCorrect()) {
            Toast.makeText(this, getResources().getString(R.string.incorrect_camera_url), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getApplicationContext(), PreviewCameraActivity.class);
        intent.putExtra(PreviewCameraActivity.EXTRA_URL, etCameraUrl.getText().toString());
        startActivity(intent);
    }

    public void updateUrl() {
        if(urlTemplate != null && isUrlFormCorrect() && previousUrl==null) {
                etCameraUrl.setText(urlTemplate.getFullUrl(this));
        }
    }

    public void loadCameraToForm() {
        etCameraName.setText(this.cameraToLoad.getName());
        etUserName.setText(this.cameraToLoad.getUserName());
        etPassword.setText(this.cameraToLoad.getPassword());
        etIp.setText(this.cameraToLoad.getAddressIp());
        etPort.setText(this.cameraToLoad.getPort());
        etChannel.setText(this.cameraToLoad.getChannel());
        try {
            spinStreamType.setSelection(Integer.parseInt(this.cameraToLoad.getStream()));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            spinStreamType.setSelection(0);
        }
        Log.d(TAG, String.valueOf(dataManager.isUrlsTemplatesLoaded()));
        if(dataManager.isUrlsTemplatesLoaded()) this.onDataLoaded();

        if(llMoreForm.getVisibility() != View.VISIBLE && this.cameraToLoad.getProducer().length() > 0) {
            buttonShowMore.callOnClick();
        }
        etServerUrl.setText(this.cameraToLoad.getServerUrl());

        etCameraUrl.setText(this.cameraToLoad.getUrl());
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
}
