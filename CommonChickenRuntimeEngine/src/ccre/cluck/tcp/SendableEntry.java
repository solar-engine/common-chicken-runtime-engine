/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ccre.cluck.tcp;

public class SendableEntry {

    public String src, dst;
    public byte[] data;

    public SendableEntry(String src, String dst, byte[] data) {
        this.src = src;
        this.dst = dst;
        this.data = data;
    }
}
