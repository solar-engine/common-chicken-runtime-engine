/*
 * Copyright 2013 Colby Skeggs
 * 
 * This file is part of the CCRE, the Common Chicken Runtime Engine.
 * 
 * The CCRE is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * The CCRE is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the CCRE.  If not, see <http://www.gnu.org/licenses/>.
 */
package ccre.obsidian;

/**
 * All the default GPIO channels and their line numbers.
 *
 * @author skeggsc
 */
public interface GPIOChannels {

    public static final int P9_22 = 2;
    public static final int UART2_RXD = 2;
    public static final int P9_21 = 3;
    public static final int UART2_TXD = 3;
    public static final int I2C1_SDA = 4;
    public static final int P9_18 = 4;
    public static final int I2C1_SCL = 5;
    public static final int P9_17 = 5;
    public static final int GPIO0_7 = 7;
    public static final int P9_42 = 7;
    public static final int P8_35 = 8;
    public static final int UART4_CTSN = 8;
    public static final int P8_33 = 9;
    public static final int UART4_RTSN = 9;
    public static final int P8_31 = 10;
    public static final int UART5_CTSN = 10;
    public static final int P8_32 = 11;
    public static final int UART5_RTSN = 11;
    public static final int I2C2_SDA = 12;
    public static final int P9_20 = 12;
    public static final int I2C2_SCL = 13;
    public static final int P9_19 = 13;
    public static final int P9_26 = 14;
    public static final int UART1_RXD = 14;
    public static final int P9_24 = 15;
    public static final int UART1_TXD = 15;
    public static final int CLKOUT2 = 20;
    public static final int P9_41 = 20;
    public static final int EHRPWM2A = 22;
    public static final int P8_19 = 22;
    public static final int EHRPWM2B = 23;
    public static final int P8_13 = 23;
    public static final int GPIO0_26 = 26;
    public static final int P8_14 = 26;
    public static final int GPIO0_27 = 27;
    public static final int P8_17 = 27;
    public static final int P9_11 = 30;
    public static final int UART4_RXD = 30;
    public static final int P9_13 = 31;
    public static final int UART4_TXD = 31;
    public static final int GPIO1_0 = 32;
    public static final int P8_25 = 32;
    public static final int GPIO1_1 = 33;
    public static final int P8_24 = 33;
    public static final int GPIO1_2 = 34;
    public static final int P8_5 = 34;
    public static final int GPIO1_3 = 35;
    public static final int P8_6 = 35;
    public static final int GPIO1_4 = 36;
    public static final int P8_23 = 36;
    public static final int GPIO1_5 = 37;
    public static final int P8_22 = 37;
    public static final int GPIO1_6 = 38;
    public static final int P8_3 = 38;
    public static final int GPIO1_7 = 39;
    public static final int P8_4 = 39;
    public static final int GPIO1_12 = 44;
    public static final int P8_12 = 44;
    public static final int GPIO1_13 = 45;
    public static final int P8_11 = 45;
    public static final int GPIO1_14 = 46;
    public static final int P8_16 = 46;
    public static final int GPIO1_15 = 47;
    public static final int P8_15 = 47;
    public static final int GPIO1_16 = 48;
    public static final int P9_15 = 48;
    public static final int GPIO1_17 = 49;
    public static final int P9_23 = 49;
    public static final int EHRPWM1A = 50;
    public static final int P9_14 = 50;
    public static final int EHRPWM1B = 51;
    public static final int P9_16 = 51;
    public static final int USR0 = 53;
    public static final int USR1 = 54;
    public static final int USR2 = 55;
    public static final int USR3 = 56;
    public static final int GPIO1_28 = 60;
    public static final int P9_12 = 60;
    public static final int GPIO1_29 = 61;
    public static final int P8_26 = 61;
    public static final int GPIO1_30 = 62;
    public static final int P8_21 = 62;
    public static final int GPIO1_31 = 63;
    public static final int P8_20 = 63;
    public static final int GPIO2_1 = 65;
    public static final int P8_18 = 65;
    public static final int P8_7 = 66;
    public static final int TIMER4 = 66;
    public static final int P8_8 = 67;
    public static final int TIMER7 = 67;
    public static final int P8_10 = 68;
    public static final int TIMER6 = 68;
    public static final int P8_9 = 69;
    public static final int TIMER5 = 69;
    public static final int GPIO2_6 = 70;
    public static final int P8_45 = 70;
    public static final int GPIO2_7 = 71;
    public static final int P8_46 = 71;
    public static final int GPIO2_8 = 72;
    public static final int P8_43 = 72;
    public static final int GPIO2_9 = 73;
    public static final int P8_44 = 73;
    public static final int GPIO2_10 = 74;
    public static final int P8_41 = 74;
    public static final int GPIO2_11 = 75;
    public static final int P8_42 = 75;
    public static final int GPIO2_12 = 76;
    public static final int P8_39 = 76;
    public static final int GPIO2_13 = 77;
    public static final int P8_40 = 77;
    public static final int P8_37 = 78;
    public static final int UART5_TXD = 78;
    public static final int P8_38 = 79;
    public static final int UART5_RXD = 79;
    public static final int P8_36 = 80;
    public static final int UART3_CTSN = 80;
    public static final int P8_34 = 81;
    public static final int UART3_RTSN = 81;
    public static final int GPIO2_22 = 86;
    public static final int P8_27 = 86;
    public static final int GPIO2_23 = 87;
    public static final int P8_29 = 87;
    public static final int GPIO2_24 = 88;
    public static final int P8_28 = 88;
    public static final int GPIO2_25 = 89;
    public static final int P8_30 = 89;
    public static final int P9_31 = 110;
    public static final int SPI1_SCLK = 110;
    public static final int P9_29 = 111;
    public static final int SPI1_D0 = 111;
    public static final int P9_30 = 112;
    public static final int SPI1_D1 = 112;
    public static final int P9_28 = 113;
    public static final int SPI1_CS0 = 113;
    public static final int GPIO3_19 = 115;
    public static final int P9_27 = 115;
    public static final int GPIO3_21 = 117;
    public static final int P9_25 = 117;
}
