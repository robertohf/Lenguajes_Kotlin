package si.dime.kotlin.tutorials.rest.booklibrary

import com.beust.klaxon.*
import com.github.kittinunf.fuel.httpGet
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.bind.annotation.*
import java.awt.image.BufferedImage
import java.io.*
import java.util.*
import javax.imageio.ImageIO
import java.io.ByteArrayInputStream
import sun.misc.BASE64Decoder
import java.util.Base64
import java.nio.file.Files


@SpringBootApplication
open class App {
}

class gmaps {
    lateinit var origen: String
    lateinit var destino: String

}

class image {
    lateinit var nombre: String
    lateinit var data: String
    lateinit var tamaño: tamaño
}

class tamaño{
    var ancho: Int = 0
    var alto: Int = 0
}


@RestController
class Ejercicio1 {
    @RequestMapping("/ejercicio1", method = arrayOf(RequestMethod.POST))
    fun directions(@RequestBody gmaps: gmaps): Any? {

        val origen = gmaps.origen
        val destino = gmaps.destino

        val direction_request = "https://maps.googleapis.com/maps/api/directions/json?origin=$origen&destination=$destino&key=AIzaSyBmelZAhVTODrw_gjtueTuHEs9Aka_z9nM"

        val (request, response, result) = direction_request.httpGet().responseString()

        val res = result.get()

        val parser : Parser = Parser()
        val stringBuilder: StringBuilder = StringBuilder(res)
        val json: JsonObject = parser.parse(stringBuilder) as JsonObject

        val arr = ArrayList(JsonArray<Any?>())

        val start_latlng = (json["routes"] as JsonArray<*>)["legs"]["steps"]["start_location"]
        val end_latlng = (json["routes"] as JsonArray<*>)["legs"]["steps"]["end_location"]

        var x = 0
        while (x < start_latlng.size && x < end_latlng.size) {
            arr.add(start_latlng.value.removeAt(x))
            arr.add(end_latlng.value.removeAt(x))
            x++
        }

        return arr
    }
}

@RestController
class Ejercicio2 {
    @RequestMapping("/ejercicio2", method = arrayOf(RequestMethod.POST))
    fun restaurants(@RequestBody gmaps: gmaps) : Any? {

        val origen = gmaps.origen

        val geocode_request = "https://maps.googleapis.com/maps/api/geocode/json?address=$origen&key=AIzaSyBmelZAhVTODrw_gjtueTuHEs9Aka_z9nM"
        val (request, response, result) = geocode_request.httpGet().responseString()

        val res = result.get()

        val parser : Parser = Parser()
        val stringBuilder: StringBuilder = StringBuilder(res)
        val json: JsonObject = parser.parse(stringBuilder) as JsonObject

        val lat = (json["results"] as JsonArray<*>)["geometry"]["location"]["lat"]
        val lng = (json["results"] as JsonArray<*>)["geometry"]["location"]["lng"]

        val lat_str = lat.value.removeAt(0)
        val lng_str = lng.value.removeAt(0)

        val nearby_place_request = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=$lat_str,$lng_str&radius=500&type=restaurant&key=AIzaSyBmelZAhVTODrw_gjtueTuHEs9Aka_z9nM"
        val (request2, response2, result2) = nearby_place_request.httpGet().responseString()

        val res2 = result2.get()

        val stringBuilder2: StringBuilder = StringBuilder(res2)
        val json2: JsonObject = parser.parse(stringBuilder2) as JsonObject

        val arr = ArrayList(JsonArray<Any?>())

        val name = (json2["results"] as JsonArray<*>)["name"]
        val latlng = (json2["results"] as JsonArray<*>)["geometry"]["location"]

        var x = 0
        while (x < latlng.size && x < name.size) {
            arr.add(name.value.removeAt(x))
            arr.add(latlng.value.removeAt(x))
            x++
        }
        return arr
    }
}

@RestController
class Ejercicio3 {
    @RequestMapping("/ejercicio3", method = arrayOf(RequestMethod.POST))
    fun greyScailing(@RequestBody image: image) : Any? {

        val data = image.data

        val image2: BufferedImage?
        val imageByte: ByteArray

        val decoder = BASE64Decoder()
        imageByte = decoder.decodeBuffer(data)
        val bis = ByteArrayInputStream(imageByte)
        image2 = ImageIO.read(bis)

        for(x in 0..image2.width - 1){
            for(y in 0..image2.height - 1){
                val rgb = image2.getRGB(x, y)

                val r = (rgb shr 16) and 0xFF
                val g = (rgb shr 8) and 0xFF
                val b = (rgb and 0xFF)

                val grayLevel = ((r + g + b) / 3)
                val gray = grayLevel shl 16 or (grayLevel shl 8) or grayLevel

                image2.setRGB(x, y, gray)
            }
        }

        val outputfile = File("img_gs.bmp")
        ImageIO.write(image2, "bmp", outputfile)

        val fi = File("img_gs.bmp")
        val byte : ByteArray = Files.readAllBytes(fi.toPath())
        val encoder = Base64.getEncoder().encodeToString(byte)

        val json_string = "{\"nombre\":\"" + image.nombre + "\",\"data\":\""+encoder+"\"}"

        val parser : Parser = Parser()
        val stringBuilder: StringBuilder = StringBuilder(json_string)
        val json: JsonObject = parser.parse(stringBuilder) as JsonObject

        return json
    }
}

@RestController
class Ejercicio4 {
    @RequestMapping("/ejercicio4", method = arrayOf(RequestMethod.POST))
    fun redux(@RequestBody req: image) : Any? {

        val data = req.data

        val image2: BufferedImage?
        val imageByte: ByteArray

        val decoder = BASE64Decoder()
        imageByte = decoder.decodeBuffer(data)
        val bis = ByteArrayInputStream(imageByte)
        image2 = ImageIO.read(bis)

        val w = image2.width
        val h = image2.height

        val factorW = w/req.tamaño.ancho
        val factorH = h/req.tamaño.alto

        val newW: Int = (w/factorW)
        val newH: Int = (h/factorH)

        val new_img: BufferedImage = BufferedImage(newW,newH,1)

        for(x in 0..newW - 1){
            for(y in 0..newH - 1){
                val pixel = image2.getRGB((x * factorW), (y * factorH))
                new_img.setRGB(x, y, pixel)
            }
        }

        val outputfile = File("img_rdx.bmp")
        ImageIO.write(new_img, "bmp", outputfile)

        val fi = File("img_rdx.bmp")
        val byte : ByteArray = Files.readAllBytes(fi.toPath())
        val encoder = Base64.getEncoder().encodeToString(byte)

        val json_string = "{\"nombre\":\"" + req.nombre + "\",\"data\":\""+encoder+"\"}"

        val parser : Parser = Parser()
        val stringBuilder: StringBuilder = StringBuilder(json_string)
        val json: JsonObject = parser.parse(stringBuilder) as JsonObject

        return json
    }
}


fun main(args: Array<String>) {
    SpringApplication.run(App::class.java, *args)
}