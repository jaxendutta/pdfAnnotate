package com.example.pdfAnnotate

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageView
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt


@SuppressLint("AppCompatCustomView")
class PDFimage // constructor
    (context: Context?) : ImageView(context){
    private var path: Path? = null
    private var bitmap: Bitmap? = null
    private var pen = Paint()
    private val bound = RectF()
    private var highlighter = Paint()

    init {
        for (i in 0 until Total) {
            pages.add(ArrayList())
        }
        annos = pages[0]
        pen.apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 6.0F
            color = Color.BLUE
        }
        highlighter.apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 28.5F
            setARGB(70,255,255,2)
        }
    }
    private var x1 = 0f
    private var x2 = 0f
    private var y1 = 0f
    private var y2 = 0f
    private var _x1 = 0f
    private var _y1 = 0f
    private var _x2 = 0f
    private var _y2 = 0f
    private var mid_x = -1f
    private var mid_y = -1f
    private var _mid_x = -1f
    private var _mid_y = -1f
    private var p1_id = 0
    private var p1_index = 0
    private var p2_id = 0
    private var p2_index = 0

    private var currentMatrix = Matrix()
    private var inverse = Matrix()
    // capture touch events (down/move/up) to create a path
    // and use that to create a stroke that we can draw
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var inverted: FloatArray
        when(event.pointerCount) {
            1 -> {
                inverse = Matrix()
                currentMatrix.invert(inverse)
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        path = Path()
                        path!!.moveTo(event.x, event.y)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        path!!.lineTo(event.x, event.y)
                        if (Tool == "eraser") {
                            erase()
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        Log.d("LOGNAME", "Action up2")
                        if (Tool != "eraser" && Tool != "Hand") {
                            path?.let { Anno(it, Tool, true) }?.let { annos.add(it) }
                            path = null
                            undo.push(annos[annos.size - 1])
                        }
                    }
                }
            }
            else -> {
                //using this code from the Android/zoompan
                Tool = "Hand"
                p1_id = event.getPointerId(0)
                p1_index = event.findPointerIndex(p1_id)

                // mapPoints returns values in-place
                inverted = floatArrayOf(event.getX(p1_index), event.getY(p1_index))
                inverse.mapPoints(inverted)

                // first pass, initialize the old == current value
                if (_x1 < 0 || _y1 < 0) {
                    x1 = inverted[0]
                    _x1 = x1
                    y1 = inverted[1]
                    _y1 = y1
                } else {
                    _x1 = x1
                    _y1 = y1
                    x1 = inverted[0]
                    y1 = inverted[1]
                }
                p2_id = event.getPointerId(1)
                p2_index = event.findPointerIndex(p2_id)
                inverted = floatArrayOf(event.getX(p2_index), event.getY(p2_index))
                inverse.mapPoints(inverted)
                if (_x2 < 0 || _y2 < 0) {
                    x2 = inverted[0]
                    _x2 = x2
                    y2 = inverted[1]
                    _y2 = y2
                } else {
                    _x2 = x2
                    _y2 = y2
                    x2 = inverted[0]
                    y2 = inverted[1]
                }
                mid_x = (x1 + x2) / 2
                mid_y = (y1 + y2) / 2
                _mid_x = (_x1 + _x2) / 2
                _mid_y = (_y1 + _y2) / 2
                val _d =
                    sqrt((_x1 - _x2).toDouble().pow(2.0) + (_y1 - _y2).toDouble().pow(2.0))
                        .toFloat()
                val d = sqrt((x1 - x2).toDouble().pow(2.0) + (y1 - y2).toDouble().pow(2.0))
                    .toFloat()
                when (event.action) {
                    MotionEvent.ACTION_MOVE -> {
                        val dx = mid_x - _mid_x
                        val dy = mid_y - _mid_y
                        currentMatrix.preTranslate(dx, dy)
                        var scale = d / _d
                        scale = max(0f, scale)
                        currentMatrix.preScale(scale, scale, mid_x, mid_y)
                        // reset on up
                    }
                    MotionEvent.ACTION_UP -> {
                        _x1 = -1f
                        _y1 = -1f
                        _x2 = -1f
                        _y2 = -1f
                        _mid_x = -1f
                        _mid_y = -1f
                    }
                    else -> {
                        Log.d("LOGNAME", "Action down222")
                    }
                }
            }
        }
        return true
    }

    fun setImage(bitmap: Bitmap?, i: Int) {
        this.bitmap = bitmap
        annos = pages[i]
    }

    private fun erase(){
        val eraseRegion = Region()
        path?.let { createReligion(it, eraseRegion, bound) }
        for (i in annos) {
            if (!i.visibility) continue
            val pathRegion = Region()
            createReligion(i.paths, pathRegion, bound)
            if (pathRegion.op(eraseRegion, Region.Op.INTERSECT)) {
                i.visibility = false
                undo.push(i)
            }
        }
    }


    override fun onDraw(canvas: Canvas) {
        if (bitmap != null) {
            setImageBitmap(bitmap)
        }
        canvas.concat(currentMatrix)
        for (i in annos) {
            if (i.visibility) {
                if (i.tool == "pen") {
                    canvas.drawPath(i.paths, pen)
                } else if (i.tool == "highlight") {
                    canvas.drawPath(i.paths, highlighter)
                }
            }
        }
        if (path != null && Tool != "eraser"&& Tool != "Hand") {
            if(Tool == "pen") {
                canvas.drawPath(path!!, pen)
            }else if(Tool == "highlight"){
                canvas.drawPath(path!!, highlighter)
            }
        }
        super.onDraw(canvas)
    }
}
