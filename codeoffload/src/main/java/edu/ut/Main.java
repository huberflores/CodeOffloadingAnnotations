/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.ut;

import java.awt.GraphicsEnvironment;

/**
 *
 * @author Barca
 */
public class Main {
    public static void main(String[] args) {
        MainFrame mf = new MainFrame();
        mf.setLocation((GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint().x - mf.getWidth() / 2), 10);
        mf.setVisible(true);
    }
}
