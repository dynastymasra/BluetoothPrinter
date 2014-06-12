package id.dynastymasra.www;

import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;

public class PrinterCommandTranslator {
    public static final int RESET = 0;
    public static final int FEED = 1;
    public static final int LEFT = 2;
    public static final int WIDTH = 3;
    public static final int OPPOSITE = 4;
    public static final int LINESP = 5;
    public static final int ALIGN = 6;
    public static final int DUP = 7;
    public static final int BARCODE = 8;
    public static final int STATE = 9;
    public static final int BAR_WIDTH = 10;
    public static final int BAR_HEIGHT = 11;
    public static final int BAR_HRI = 12;
    public static final int FORMAT = 13;

    public static final int AT_LEFT = 0;
    public static final int AT_CENTER = 1;
    public static final int AT_RIGHT = 2;
    private static final String DEFAULT_FONT = "gb2312";
    private static final int NORMAL_WIDTH_IN_CHAR = 24;

    private int MakeFormat(boolean width, boolean height, boolean bold, boolean underline, boolean minifont) {
        byte com[] = new byte[1];
        com[0] = 0;
        if (width)
            com[0] |= 32;
        if (height)
            com[0] |= 16;
        if (bold)
            com[0] |= 8;
        if (underline)
            com[0] |= 128;
        if (minifont)
            com[0] |= 0;
        return com[0];
    }

    private byte[] MakeComm(int iType, int para1, int para2, byte iLength[]) {
        byte mBuff[] = new byte[20];
        int mLen = 0;
        switch (iType) {
            default:
                break;

            case RESET:
                mBuff[0] = 27;
                mBuff[1] = 64;
                mLen = 2;
                break;

            case FEED:
                mBuff[0] = 27;
                mBuff[1] = 74;
                mBuff[2] = (byte) para1;
                mLen = 3;
                break;

            case LEFT:
                mBuff[0] = 29;
                mBuff[1] = 76;
                mBuff[2] = (byte) (para1 / 256);
                mBuff[3] = (byte) (para1 % 256);
                mLen = 4;
                break;

            case WIDTH:
                mBuff[0] = 29;
                mBuff[1] = 87;
                mBuff[2] = (byte) (para1 / 256);
                mBuff[3] = (byte) (para1 % 256);
                mLen = 4;
                break;

            case OPPOSITE:
                mBuff[0] = 29;
                mBuff[1] = 66;
                mBuff[2] = (byte) para1;
                mLen = 3;
                break;

            case LINESP:
                if (1 == para2) {
                    mBuff[0] = 27;
                    mBuff[1] = 50;
                    mLen = 2;
                } else {
                    mBuff[0] = 27;
                    mBuff[1] = 51;
                    mBuff[2] = (byte) para1;
                    mLen = 3;
                }
                break;

            case ALIGN:
                mBuff[0] = 27;
                mBuff[1] = 97;
                mBuff[2] = (byte) para1;
                mLen = 3;
                break;

            case DUP:
                mBuff[0] = 27;
                mBuff[1] = 71;
                mBuff[2] = (byte) para1;
                mLen = 3;
                break;

            case BARCODE:
                mBuff[0] = 29;
                mBuff[1] = 107;
                mBuff[2] = (byte) para1;
                mBuff[3] = (byte) para2;
                mLen = 4;
                break;

            case STATE:
                mBuff[0] = 29;
                mBuff[1] = 119;
                mBuff[2] = (byte) para1;
                mLen = 3;
                break;

            case BAR_WIDTH:
                mBuff[0] = 29;
                mBuff[1] = 104;
                mBuff[2] = (byte) para1;
                mLen = 3;
                break;

            case BAR_HEIGHT:
                mBuff[0] = 29;
                mBuff[1] = 72;
                mBuff[2] = (byte) para1;
                mLen = 3;
                break;

            case BAR_HRI:
                mBuff[0] = 27;
                mBuff[1] = 118;
                mLen = 2;
                break;

            case FORMAT:
                mBuff[0] = 27;
                mBuff[1] = 33;
                mBuff[2] = (byte) para1;
                mLen = 3;
                break;
        }
        iLength[0] = (byte) mLen;
        return mBuff;
    }

    public byte[] Reset() {
        byte bLen[] = new byte[1];
        byte cmd[] = MakeComm(0, 0, 0, bLen);
        return cmd;
    }

    public byte[] AlignLeft() {
        return Align(AT_LEFT);
    }

    public byte[] AlignRight() {
        return Align(AT_RIGHT);
    }

    public byte[] AlignCenter() {
        return Align(AT_CENTER);
    }

    private byte[] Align(int alignType) {
        byte bLen[] = new byte[1];
        byte cmd[] = MakeComm(6, alignType, 0, bLen);
        return cmd;
    }

    public byte[] Format(boolean width, boolean height, boolean bold, boolean underline, boolean minifont) {
        byte bLen[] = new byte[1];
        int datacom = MakeFormat(width, height, bold, underline, minifont);
        byte data[] = MakeComm(13, datacom, 0, bLen);
        return data;
    }

    public byte[] FeedLines(int num) {
        byte bLen[] = new byte[1];
        byte data[] = MakeComm(1, num, 0, bLen);
        return data;
    }

    public byte[] Translate(String textToPrint) {
        try {
            return textToPrint.getBytes(DEFAULT_FONT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return textToPrint.getBytes();
        }

    }

    public byte[] toNormalLeft(String strData) {
        byte[] align = this.AlignLeft();
        byte[] format = this.Format(false, false, false, false, false);
        byte[] data = this.Translate(strData + "\n");
        byte[] result = new byte[align.length + format.length + data.length];
        System.arraycopy(align, 0, result, 0, align.length);
        System.arraycopy(format, 0, result, align.length, format.length);
        System.arraycopy(data, 0, result, align.length + format.length, data.length);
        return result;
    }

    public byte[] toMiniLeft(String strData) {
        byte[] align = this.AlignLeft();
        byte[] format = this.Format(false, false, false, false, true);
        byte[] data = this.Translate(strData + "\n");
        byte[] result = new byte[align.length + format.length + data.length];
        System.arraycopy(align, 0, result, 0, align.length);
        System.arraycopy(format, 0, result, align.length, format.length);
        System.arraycopy(data, 0, result, align.length + format.length, data.length);
        return result;
    }

    public byte[] toNormalCenter(String strData) {
        byte[] align = this.AlignCenter();
        byte[] format = this.Format(false, false, false, false, false);
        byte[] data = this.Translate(strData + "\n");
        byte[] result = new byte[align.length + format.length + data.length];
        System.arraycopy(align, 0, result, 0, align.length);
        System.arraycopy(format, 0, result, align.length, format.length);
        System.arraycopy(data, 0, result, align.length + format.length, data.length);
        return result;
    }

    public byte[] toNormalRepeatTillEnd(char c) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < NORMAL_WIDTH_IN_CHAR; i++) {
            builder.append(c);
        }
        return this.toNormalLeft(builder.toString());
    }

    public byte[] toNormalCenterAll(String strData) {
        String[] split24 = take24Char(new String[0], strData);
        byte[] result = new byte[0];
        for (String s : split24) {
            byte[] translated = this.toNormalCenter(s);
            result = gabungkanDuaArray(result, translated);
        }
        return result;
    }

    private String[] take24Char(String[] penampung, String toTake) {
        String[] take = new String[1];
        String nextToTake = "";
        if (toTake.length() < NORMAL_WIDTH_IN_CHAR) {
            take[0] = toTake;
            return gabungkanDuaArray(penampung, take);
        } else {
            take[0] = toTake.substring(0, NORMAL_WIDTH_IN_CHAR - 1);
            nextToTake = toTake.substring(NORMAL_WIDTH_IN_CHAR, toTake.length());
            return take24Char(gabungkanDuaArray(penampung, take), nextToTake);
        }

    }

    private String[] gabungkanDuaArray(String[] penampung, String[] take) {
        String[] gabung = new String[penampung.length + take.length];
        System.arraycopy(penampung, 0, gabung, 0, penampung.length);
        System.arraycopy(take, 0, gabung, penampung.length, take.length);
        return gabung;
    }

    private byte[] gabungkanDuaArray(byte[] penampung, byte[] take) {
        byte[] gabung = new byte[penampung.length + take.length];
        System.arraycopy(penampung, 0, gabung, 0, penampung.length);
        System.arraycopy(take, 0, gabung, penampung.length, take.length);
        return gabung;
    }

    public byte[] toNormalTwoColumn(String desc, double number) {
        StringBuilder build = new StringBuilder();
        for (int i = 0; i < NORMAL_WIDTH_IN_CHAR; i++) {
            build.append(' ');
        }
        if (desc.length() > NORMAL_WIDTH_IN_CHAR) {
            String take = desc.substring(0, NORMAL_WIDTH_IN_CHAR);
            build.replace(0, NORMAL_WIDTH_IN_CHAR, take);
        } else {
            build.replace(0, desc.length(), desc);
        }
        String strNum = "  " + numberToString(number);
        build.replace(NORMAL_WIDTH_IN_CHAR - strNum.length(), NORMAL_WIDTH_IN_CHAR, strNum);
        return this.toNormalLeft(build.toString());
    }

    public String numberToString(double number) {
        return NumberFormat.getInstance().format(number);
    }

    // order checker
    public byte[] toNormalTwoColumn2(double number, String desc) {
        StringBuilder build = new StringBuilder();
        for (int i = 0; i < NORMAL_WIDTH_IN_CHAR; i++) {
            build.append(' ');
        }
        if (desc.length() > NORMAL_WIDTH_IN_CHAR) {
            String take = desc.substring(0, NORMAL_WIDTH_IN_CHAR);
            build.replace(0, NORMAL_WIDTH_IN_CHAR, take);
        } else {
            build.replace(0, desc.length(), desc);
        }
        String strNum = "  " + numberToString(number);
        build.replace(NORMAL_WIDTH_IN_CHAR - strNum.length(), NORMAL_WIDTH_IN_CHAR, strNum);
        return this.toNormalLeft(build.toString());
    }
}

