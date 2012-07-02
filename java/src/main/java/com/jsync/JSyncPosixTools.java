package com.jsync;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Structure;

import java.io.UnsupportedEncodingException;


public class JSyncPosixTools {

    static int UREAD  = 00400;
    static int UWRITE = 00200;
    static int UEXEC  = 00100;

    static int GREAD  = 00040;
    static int GWRITE = 00020;
    static int GEXEC  = 00010;

    static int OREAD  = 00004;
    static int OWRITE = 00002;
    static int OEXEC  = 00001;

    static int PATH_MAX = 5120;

    public static boolean hasPerm(int perm, int mode) {
        return ((perm & mode) == perm);
    }

    public static String toModeStr(int mode) {
        String out = "";

        if (hasPerm(UREAD,mode)) out += "r";
        else                     out += "-";

        if (hasPerm(UWRITE,mode)) out += "w";
        else                     out += "-";

        if (hasPerm(UEXEC,mode)) out += "x";
        else                     out += "-";

        if (hasPerm(GREAD,mode)) out += "r";
        else                     out += "-";

        if (hasPerm(GWRITE,mode)) out += "w";
        else                     out += "-";

        if (hasPerm(GEXEC,mode)) out += "x";
        else                     out += "-";

        if (hasPerm(OREAD,mode)) out += "r";
        else                     out += "-";

        if (hasPerm(OWRITE,mode)) out += "w";
        else                     out += "-";

        if (hasPerm(OEXEC,mode)) out += "x";
        else                     out += "-";

        return out;
    }

    public static class ShortStat extends Structure {
        public int mode;
        public int uid;
        public int gid;
        public NativeLong atime;
        public NativeLong mtime;
    }

    public static class UTimes extends Structure {
        public NativeLong actime;
        public NativeLong modtime;
    }

    interface CLibrary extends Library {
        public int chmod(String path, int mode);
        public int chown(String path, int uid, int gid);
        public int utime(String path, JSyncPosixTools.UTimes times);
    }

    interface JSyncLibrary extends Library {
        public int shortstat(String path, ShortStat buf);
        public int readlink(String path, byte[] buf, int buf_size);
        public int symlink(String path, String target);
    }

    private static CLibrary libc = (CLibrary) Native.loadLibrary("c", CLibrary.class);
    private static JSyncLibrary libjsync = (JSyncLibrary) Native.loadLibrary("jsync", JSyncLibrary.class);

    public static String getLinkTarget(String path) {
        byte[] buf = new byte[PATH_MAX];
        int size = libjsync.readlink(path, buf, PATH_MAX);
        if (size > 0) {
            String out = "";
            for (int i = 0; i < size; i++) {
                out += (char) buf[i];
            }
            return out;
        }
        return null;
    }

    public static int createSymlink(String path, String target) {
        return libjsync.symlink(target, path);
    }

    public static int chmod(String path, int mode) {
        return libc.chmod(path, mode);
    }

    public static int chown(String path, int uid, int gid) {
        return libc.chown(path, uid, gid);
    }

    public static ShortStat stat(String path) {
        JSyncPosixTools.ShortStat s = new JSyncPosixTools.ShortStat();
        libjsync.shortstat(path, s);
        return s;
    }

    public static int setTimes(String path, long atime, long mtime) {
        JSyncPosixTools.UTimes times = new JSyncPosixTools.UTimes();
        times.actime = new NativeLong(atime);
        times.modtime = new NativeLong(mtime);
        return libc.utime(path, times);
    }

}
