package org.jclass.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by hasee on 2017/5/15.
 */
public final class PropsUtil {

    public static Properties loadProps(String fileName){
        Properties props = null;
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        try {
            if (null == is){
                throw new FileNotFoundException("can not find the file:"+fileName);
            }
            props = new Properties();
            props.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return props;
    }

    public final static String getPropString(Properties props, String key){
        return getPropString(props, key, "");
    }

    public final static String getPropString(Properties props, String key, String defaultValue){
        if (props.containsKey(key)){
            return props.getProperty(key);
        }
        return defaultValue;
    }
}
