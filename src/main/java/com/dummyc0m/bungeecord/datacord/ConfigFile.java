package com.dummyc0m.bungeecord.datacord;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.lang.reflect.Constructor;

/**
 * Created by Dummyc0m on 3/3/15.
 */
public class ConfigFile {
    private File file;
    private final Gson gson;
    private Object settings;

    public ConfigFile(File dataFolder, String file, Class<?> settingsClass) {
        this(dataFolder, file, settingsClass, new GsonBuilder()
                .disableHtmlEscaping()
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .setPrettyPrinting()
                .serializeNulls()
                .create());
    }

    public ConfigFile(File dataFolder, String file, Class<?> settingsClass, Gson gson) {
        this.gson = gson;
        try {
            if (!dataFolder.exists() && !dataFolder.mkdir()) {
                throw new IOException("An error occurred when trying to create configuration folder");
            }
            this.file = new File(dataFolder.getAbsolutePath(), file);
            if(this.file.exists() || this.file.createNewFile()) {
                BufferedReader bReader = new BufferedReader(new FileReader(this.file));
                this.settings = this.gson.fromJson(bReader, settingsClass);
                if(settings == null) {
                    useDefault(settingsClass);
                }
                bReader.close();
            } else {
                throw new IOException("An error occurred when trying to instantiate a configuration");
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void useDefault(Class<?> settingsClass){
        try {
            Constructor<?> constructor = settingsClass.getConstructor();
            this.settings = constructor.newInstance();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public Object getSettings(){
        return this.settings;
    }

    public Object save() {
        try {
            FileWriter fWriter = new FileWriter(file, false);
            fWriter.write(this.gson.toJson(settings));
            fWriter.flush();
            fWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return settings;
    }
}
