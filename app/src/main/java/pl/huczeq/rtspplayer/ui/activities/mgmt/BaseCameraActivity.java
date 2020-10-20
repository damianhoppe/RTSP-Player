package pl.huczeq.rtspplayer.ui.activities.mgmt;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
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
import pl.huczeq.rtspplayer.ui.activities.base.BaseActivity;
import pl.huczeq.rtspplayer.adapters.ModelsSpinnerAdapter;
import pl.huczeq.rtspplayer.adapters.ProducersSpinnerAdapter;
import pl.huczeq.rtspplayer.interfaces.RightDrawableOnTouchListener;
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

    EditText etCameraName, etCameraUrl, etUserName, etPassword, etIp, etPort, etChannel, etServerUrl;
    TextView tvProducers, tvModels;
    Spinner spinStreamType, spinProducers, spinModel;
    LinearLayout llMoreForm;
    ProgressBar pbLoadingProducers;

    ProducersSpinnerAdapter producersAdapter;
    ModelsSpinnerAdapter modelsAdapter;

    Button buttonShowMore;
    FloatingActionButton buttonAddCamera;
    FloatingActionButton buttonStartCameraPreview;

    UrlTemplate urlTemplate;
    String previousUrl;

    Camera cameraToLoad;

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
        setContentView(R.layout.activity_addedit_camera);

        producersAdapter = new ProducersSpinnerAdapter(getBaseContext(), new ArrayList<Producer>());
        modelsAdapter = new ModelsSpinnerAdapter(getBaseContext(), new ArrayList<Model>());
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
                if(urlTemplate != null && previousUrl != null) {
                    if(!previousUrl.equalsIgnoreCase(etCameraUrl.getText().toString())) {
                        spinProducers.setSelection(0);
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

        spinProducers.setAdapter(producersAdapter);
        spinProducers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                onProducerSelected(producersAdapter.getItem(i));
                updateUrl();
            }
            @Override public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        spinModel.setAdapter(modelsAdapter);
        spinModel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                onModelSelected(modelsAdapter.getItem(i));
                updateUrl();
            }
            @Override public void onNothingSelected(AdapterView<?> adapterView) { }
        });
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
            loadProducers();
        }
    }

    @Override
    public void onDataLoaded() {
        pbLoadingProducers.setVisibility(View.GONE);
        if(this.cameraToLoad != null) {
            pbLoadingProducers.setVisibility(View.GONE);
            spinProducers.setVisibility(View.VISIBLE);
            producersAdapter.setProducersList(dataManager.getProducersList());
            if (this.cameraToLoad.getProducer().equalsIgnoreCase(ProducersSpinnerAdapter.emptyProducer.getName()) || this.cameraToLoad.getProducer().equals("") || this.cameraToLoad.getProducer() == null) {
                spinProducers.setSelection(0);
            } else {
                int index = dataManager.getUrlTemplates().getProducerIndex(this.cameraToLoad.getProducer());
                if (index == -1) {
                    spinProducers.setSelection(0);
                    return;
                }
                Producer producer = producersAdapter.getItem(index + 1);
                if (producer == null) {
                    spinProducers.setSelection(0);
                    return;
                }
                spinProducers.setSelection(index + 1);
                modelsAdapter.setModelsList(producer.getModelList());
                spinModel.setSelection(0);
                if (producer.getModelList().size() > 1) {
                    spinProducers.setVisibility(View.VISIBLE);
                }
            }
        }else {
            loadProducers();
        }
    }

    private void loadProducers() {
        if(dataManager.getProducersList().size() > 0) {
            producersAdapter.setProducersList(dataManager.getProducersList());
            spinProducers.setSelection(0);

            onProducerSelected(producersAdapter.getItem(spinProducers.getSelectedItemPosition()));

            spinProducers.setVisibility(View.VISIBLE);
            tvProducers.setVisibility(View.VISIBLE);
        }
    }
    public void onProducerSelected(Producer producer) {
        loadModels(producer);
    }
    private void loadModels(Producer producer) {
        if(producer == ProducersSpinnerAdapter.emptyProducer) {
            modelsAdapter.setModelsList(new ArrayList<Model>());
            onModelSelected(null);

            spinModel.setVisibility(View.GONE);
            tvModels.setVisibility(View.GONE);
            return;
        }

        modelsAdapter.setModelsList(producer.getModelList());
        spinModel.setSelection(0);
        onModelSelected(modelsAdapter.getItem(spinModel.getSelectedItemPosition()));

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
            Log.d(TAG, "W: " + urlTemplate.useField(UrlsTemplates.AdditionalField.ServerUrl));
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
        if(spinProducers.getSelectedItemPosition() > 0) {
            producer = producersAdapter.getItem(spinProducers.getSelectedItemPosition());
        }
        if(producer == null) {
            cam.setProducer("");
        }else {
            cam.setProducer(producer.getName());
        }

        Model model = null;
        if(spinModel.getSelectedItemPosition() >= 0) {
            model = modelsAdapter.getItem(spinModel.getSelectedItemPosition());
        }
        if(model == null) {
            cam.setProducer("");
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
        if(this.urlTemplate == null) return true;
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
        if(etCameraUrl.getText().toString().length() == 0) {
            Toast.makeText(this, getResources().getString(R.string.incorrect_camera_url), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getApplicationContext(), PreviewCameraActivity.class);
        intent.putExtra(PreviewCameraActivity.EXTRA_URL, etCameraUrl.getText().toString());
        startActivity(intent);
    }

    public void updateUrl() {
        if(isUrlFormCorrect() && urlTemplate!=null && previousUrl==null) {
            etCameraUrl.setText(urlTemplate.getFullUrl(this));
        }
    }

    public void loadToForm(Camera camera) {
        this.cameraToLoad = camera;

        etCameraName.setText(camera.getName());
        etUserName.setText(camera.getUserName());
        etPassword.setText(camera.getPassword());
        etIp.setText(camera.getAddressIp());
        etPort.setText(camera.getPort());
        etChannel.setText(camera.getChannel());
        try {
            spinStreamType.setSelection(Integer.parseInt(camera.getStream()));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            spinStreamType.setSelection(0);
        }
        Log.d(TAG, String.valueOf(dataManager.isUrlsTemplatesLoaded()));
        if(dataManager.isUrlsTemplatesLoaded()) this.onDataLoaded();

        if(llMoreForm.getVisibility() != View.VISIBLE && camera.getProducer().length() > 0) {
            buttonShowMore.callOnClick();
        }
        etServerUrl.setText(camera.getServerUrl());

        etCameraUrl.setText(camera.getUrl());
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
