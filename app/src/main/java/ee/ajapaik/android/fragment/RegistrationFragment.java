package ee.ajapaik.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import ee.ajapaik.android.fragment.util.WebFragment;
import ee.ajapaik.android.test.R;
import ee.ajapaik.android.util.Authorization;

import static ee.ajapaik.android.util.Authorization.Type.USERNAME_PASSWORD;

public class RegistrationFragment extends WebFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registration, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getSubmitButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Authorization authorization = new Authorization(
                        USERNAME_PASSWORD,
                        getUsernameInput().getText().toString(),
                        getPasswordInput().getText().toString(),
                        getFirstnameInput().getText().toString(),
                        getLastnameInput().getText().toString()
                );
                getSettings().setAuthorization(authorization);
                registerWithUsername();
            }
        });
    }

    private Button getSubmitButton() {
        return (Button)getView().findViewById(R.id.button_action_start_register);
    }

    private EditText getUsernameInput() {
        return (EditText) getView().findViewById(R.id.input_username);
    }

    private EditText getPasswordInput() {
        return (EditText) getView().findViewById(R.id.input_password);
    }

    private EditText getFirstnameInput() {
        return (EditText) getView().findViewById(R.id.input_firstname);
    }

    private EditText getLastnameInput() {
        return (EditText) getView().findViewById(R.id.input_lastname);
    }

}
