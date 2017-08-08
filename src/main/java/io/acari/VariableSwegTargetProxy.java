package io.acari;

import java.io.IOException;

public class VariableSwegTargetProxy {

    public static void main(String... args) {
        try {
            new SwegServer(Integer.parseInt(args[0]));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
