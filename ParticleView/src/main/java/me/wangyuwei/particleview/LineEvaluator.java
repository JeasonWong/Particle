package me.wangyuwei.particleview;

import android.animation.TypeEvaluator;

/**
 * 作者： 巴掌 on 16/8/27 12:06
 * Github: https://github.com/JeasonWong
 */
public class LineEvaluator implements TypeEvaluator<Particle> {

    @Override
    public Particle evaluate(float fraction, Particle startValue, Particle endValue) {
        Particle particle = new Particle();
        particle.x = startValue.x + (endValue.x - startValue.x) * fraction;
        particle.y = startValue.y + (endValue.y - startValue.y) * fraction;
        particle.radius = startValue.radius + (endValue.radius - startValue.radius) * fraction;
        return particle;
    }
}
