package ee.ajapaik.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import ee.ajapaik.android.fragment.util.WebFragment;
import ee.ajapaik.android.R;
import ee.ajapaik.android.util.Authorization;

import static ee.ajapaik.android.util.Authorization.Type.USERNAME_PASSWORD;

public class LoginFragment extends WebFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoginButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Authorization authorization = new Authorization(
                        USERNAME_PASSWORD,
                        getUsernameInput().getText().toString(),
                        getPasswordInput().getText().toString()
                );
                getSettings().setAuthorization(authorization);
                signInWithUsername();
            }
        });
    }

    private Button getLoginButton() {
        return (Button)getView().findViewById(R.id.button_action_start_login);
    }

    private EditText getUsernameInput() {
        return (EditText) getView().findViewById(R.id.input_username);
    }

    private EditText getPasswordInput() {
        return (EditText) getView().findViewById(R.id.input_password);
    }

}
