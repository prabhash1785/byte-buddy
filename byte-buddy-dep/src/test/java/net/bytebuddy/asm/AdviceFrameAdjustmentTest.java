package net.bytebuddy.asm;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AdviceFrameAdjustmentTest {

    private static final String FOO = "foo", BAR = "bar";

    @Test
    public void testTrivialAdvice() throws Exception {
        Class<?> type = new ByteBuddy()
                .redefine(ExtendedSample.class)
                .visit(new AsmVisitorWrapper.ForDeclaredMethods().readerFlags(ClassReader.EXPAND_FRAMES).writerFlags(ClassWriter.COMPUTE_MAXS).method(named(FOO), Advice.to(TrivialAdvice.class)))
                .make()
                .load(null, ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
        assertThat(type.getDeclaredMethod(FOO,
                boolean.class,
                byte.class,
                short.class,
                char.class,
                int.class,
                long.class,
                float.class,
                double.class,
                Object.class).invoke(type.newInstance(), false, (byte) 0, (short) 0, (char) 0, (short) 0, 0, 0L, 0f, 0d), is((Object) BAR));
    }

    @Test
    public void testExtendedAdvice() throws Exception {
        Class<?> type = new ByteBuddy()
                .redefine(SimpleSample.class)
                .visit(new AsmVisitorWrapper.ForDeclaredMethods().readerFlags(ClassReader.EXPAND_FRAMES).writerFlags(ClassWriter.COMPUTE_MAXS).method(named(FOO), Advice.to(ExtendedAdvice.class)))
                .make()
                .load(null, ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
        assertThat(type.getDeclaredMethod(FOO).invoke(type.newInstance()), is((Object) BAR));
    }

    @SuppressWarnings("all")
    public static class ExtendedSample {

        public static boolean trigger;

        public String foo(boolean arg1, byte arg2, short arg3, char arg4, int arg5, long arg6, float arg7, double arg8, Object arg9) {
            if (trigger) {
                return FOO;
            } else {
                return BAR;
            }
        }
    }

    public static class TrivialAdvice {

        @Advice.OnMethodEnter
        @Advice.OnMethodExit
        public static String enter() {
            if (ExtendedSample.trigger) {
                return FOO;
            } else {
                return BAR;
            }
        }
    }

    @SuppressWarnings("all")
    public static class SimpleSample {

        public static boolean trigger;

        public String foo() {
            if (trigger) {
                return FOO;
            } else {
                return BAR;
            }
        }
    }

    @SuppressWarnings("all")
    public static class ExtendedAdvice {

        @Advice.OnMethodEnter
        public static String foo(@Advice.This Object arg1, @Advice.This Object arg2) {
            if (SimpleSample.trigger) {
                return FOO;
            } else {
                return BAR;
            }
        }
    }
}
