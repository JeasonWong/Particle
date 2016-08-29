package me.wangyuwei.particle;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import me.wangyuwei.particleview.ParticleView;

public class MainActivity extends AppCompatActivity {

    private ParticleView mPv1;
    private ParticleView mPv2;
    private ParticleView mPv3;
    private ParticleView mPv4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPv1 = (ParticleView) findViewById(R.id.pv_1);
        mPv2 = (ParticleView) findViewById(R.id.pv_2);
        mPv3 = (ParticleView) findViewById(R.id.pv_3);
        mPv4 = (ParticleView) findViewById(R.id.pv_4);

        mPv1.setOnParticleAnimListener(new ParticleView.ParticleAnimListener() {
            @Override
            public void onAnimationEnd() {
                Toast.makeText(MainActivity.this, "Animation is End", Toast.LENGTH_SHORT).show();
            }
        });

        mPv1.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPv1.startAnim();
                mPv2.startAnim();
                mPv3.startAnim();
                mPv4.startAnim();
            }
        }, 200);

    }
}
