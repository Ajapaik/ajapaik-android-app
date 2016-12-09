package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import ee.ajapaik.android.fragment.AlbumsFragment;
import ee.ajapaik.android.util.WebActivity;
import ee.ajapaik.android.test.R;

public class AlbumsActivity extends WebActivity {
    public static void start(Context context) {
        context.startActivity(new Intent(context, AlbumsActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_albums);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new AlbumsFragment()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_albums, menu);

        return true;
    }
}
