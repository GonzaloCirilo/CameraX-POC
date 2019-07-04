package pe.com.redcups.cameraxapp

import android.net.Uri
import android.util.Log
import java.io.File

object ImageLoader {
    fun loadImage(file: File): Picture{
        val newItem = Picture()
        newItem.uri = Uri.fromFile(file)
        return newItem
    }

    fun loadSavedImages(dirs: Array<File>): Array<Picture>{
        var pictures: Array<Picture> = emptyArray()
        for(dir in dirs){
            if(dir.exists()){
                val files = dir.listFiles()
                for(f in files){
                    val absolutePath = f.absolutePath
                    Log.d("name",f.name)
                    Log.d("Canonical", f.canonicalPath)
                    Log.d("Absolute", f.absolutePath)
                    Log.d("path", f.path)
                    val extension = absolutePath.substring(absolutePath.lastIndexOf("."))
                    if(extension == (".jpg")){
                        pictures += loadImage(f)
                    }

                }
            }
        }
        return pictures
    }

}