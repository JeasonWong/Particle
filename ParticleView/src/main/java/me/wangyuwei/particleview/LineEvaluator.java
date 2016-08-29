package me.wangyuwei.particleview;

import android.animation.TypeEvaluator;

/**
 * 作者： 巴掌 on 16/8/27 12:06
 * Github: https://github.com/JeasonWong
 */
public class LineEvaluator implements TypeEvaluator<Particle> {

    @Override
    public Particle evaluate(float fraction, Particle startValue, Particle endValue) {
        Particle partical = new Particle();
        partical.x = startValue.x + (endValue.x - startValue.x) * fraction;
        partical.y = startValue.y + (endValue.y - startValue.y) * fraction;
        partical.radius = startValue.radius + (endValue.radius - startValue.radius) * fraction;
        return partical;
    }
}
