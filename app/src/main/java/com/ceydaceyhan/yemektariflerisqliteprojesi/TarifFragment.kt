package com.ceydaceyhan.yemektariflerisqliteprojesi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_tarif.*
import java.io.ByteArrayOutputStream


class TarifFragment : Fragment() {

    var secilenGorsel: Uri? = null
    var secilenBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tarif, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button.setOnClickListener {
            kaydet(it)
        }
        imageView.setOnClickListener {
            gorselSec(it)
        }
    }

    fun kaydet(view: View) {
        //SQLite'a kaydetme
        val yemekIsmı = yemekismiText.text.toString()
        val yemekMalzemeleri = yemekmalzemeText.text.toString()

        if (secilenBitmap != null){

            val kucukBitmap = kucukBitmapOlustur(secilenBitmap!!,300)

            val outputStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteDizisi = outputStream.toByteArray()

            try {
                context?.let {
                    val database = it.openOrCreateDatabase("Yemekler",Context.MODE_PRIVATE,null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS yemekler(id INTEGER PRIMARY KEY, yemekismi VARCHAR,yemekmalzemesi VARCHAR,gorsel BLOB)")

                    val sqlString = "INSERT INTO yemekler(yemekismi,yemekmalzemesi,gorsel) VALUES(?, ?, ?)"
                    val statement = database.compileStatement(sqlString)
                    statement.bindString(1,yemekIsmı)
                    statement.bindString(2,yemekMalzemeleri)
                    statement.bindBlob(3,byteDizisi)
                    statement.execute()

                }

            }catch (e : Exception){
                e.printStackTrace()
            }
            findNavController().navigate(R.id.action_tarifFragment_to_listeFragment2)

        }


    }

    fun gorselSec(view: View) {

        activity?.let {
            if (ContextCompat.checkSelfPermission(
                    it.applicationContext,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            } else {
                val galeriIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent, 2)
            }

        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val galeriIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent, 2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            secilenGorsel = data.data

            try {
                context?.let {
                    if (secilenGorsel != null) {
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source =
                                ImageDecoder.createSource(it.contentResolver, secilenGorsel!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            imageView.setImageBitmap(secilenBitmap)
                        } else {
                            secilenBitmap =
                                MediaStore.Images.Media.getBitmap(it.contentResolver, secilenGorsel)
                            imageView.setImageBitmap(secilenBitmap)
                        }

                    }
                }


            } catch (e: Exception) {
                e.printStackTrace()

            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun kucukBitmapOlustur(kullanicininSectigiBitmap: Bitmap, maximumBoyut: Int) : Bitmap {

        var width = kullanicininSectigiBitmap.width
        var height = kullanicininSectigiBitmap.height
        val bitmapOrani : Double = width.toDouble() / height.toDouble()

        if (bitmapOrani > 1){
            //yatay
            width = maximumBoyut
            val kisaltilmisHeight = width / bitmapOrani
            height = kisaltilmisHeight.toInt()
        }else{
            //dikey
            height = maximumBoyut
            val kisaltilmisWidth = height / bitmapOrani
            width = kisaltilmisWidth.toInt()
        }
       return Bitmap.createScaledBitmap(kullanicininSectigiBitmap,width / 2,height / 2,true)
    }

}