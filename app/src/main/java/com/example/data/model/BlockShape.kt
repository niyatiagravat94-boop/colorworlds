package com.example.data.model

data class Coordinate(val r: Int, val c: Int)

enum class ShapeType(val id: String, val category: String) {
    DOT("1x1", "Basic"),
    LINE_2_H("line_2_h", "Basic"),
    LINE_2_V("line_2_v", "Basic"),
    LINE_3_H("line_3_h", "Basic"),
    LINE_3_V("line_3_v", "Basic"),
    LINE_4_H("line_4_h", "Basic"),
    LINE_4_V("line_4_v", "Basic"),
    LINE_5_H("line_5_h", "Basic"),
    LINE_5_V("line_5_v", "Basic"),
    SQUARE_2X2("square_2x2", "Square"),
    SQUARE_3X3("square_3x3", "Square"),
    L_SMALL_TL("l_small_tl", "L-Shape"),
    L_SMALL_TR("l_small_tr", "L-Shape"),
    L_SMALL_BL("l_small_bl", "L-Shape"),
    L_SMALL_BR("l_small_br", "L-Shape"),
    L_LARGE_TL("l_large_tl", "L-Shape"),
    L_LARGE_TR("l_large_tr", "L-Shape"),
    L_LARGE_BL("l_large_bl", "L-Shape"),
    L_LARGE_BR("l_large_br", "L-Shape"),
    T_UP("t_up", "T-Shape"),
    T_DOWN("t_down", "T-Shape"),
    T_LEFT("t_left", "T-Shape"),
    T_RIGHT("t_right", "T-Shape"),
    Z_H("z_h", "Zigzag"),
    Z_V("z_v", "Zigzag"),
    S_H("s_h", "Zigzag"),
    S_V("s_v", "Zigzag"),
    CORNER_3_TL("corner_3_tl", "Corner"),
    CORNER_3_TR("corner_3_tr", "Corner"),
    CORNER_3_BL("corner_3_bl", "Corner"),
    CORNER_3_BR("corner_3_br", "Corner"),
    PLUS("plus", "Special"),
    U_SHAPE("u_shape", "Special")
}

data class BlockShape(
    val type: ShapeType,
    val color: BlockColor,
    val coordinates: List<Coordinate>,
    val rows: Int,
    val cols: Int
) {
    val size: Int get() = coordinates.size

    companion object {
        fun create(type: ShapeType, color: BlockColor): BlockShape {
            val coords: List<Coordinate> = when (type) {
                ShapeType.DOT -> listOf(Coordinate(0, 0))
                ShapeType.LINE_2_H -> listOf(Coordinate(0, 0), Coordinate(0, 1))
                ShapeType.LINE_2_V -> listOf(Coordinate(0, 0), Coordinate(1, 0))
                ShapeType.LINE_3_H -> listOf(Coordinate(0, 0), Coordinate(0, 1), Coordinate(0, 2))
                ShapeType.LINE_3_V -> listOf(Coordinate(0, 0), Coordinate(1, 0), Coordinate(2, 0))
                ShapeType.LINE_4_H -> listOf(Coordinate(0, 0), Coordinate(0, 1), Coordinate(0, 2), Coordinate(0, 3))
                ShapeType.LINE_4_V -> listOf(Coordinate(0, 0), Coordinate(1, 0), Coordinate(2, 0), Coordinate(3, 0))
                ShapeType.LINE_5_H -> listOf(Coordinate(0, 0), Coordinate(0, 1), Coordinate(0, 2), Coordinate(0, 3), Coordinate(0, 4))
                ShapeType.LINE_5_V -> listOf(Coordinate(0, 0), Coordinate(1, 0), Coordinate(2, 0), Coordinate(3, 0), Coordinate(4, 0))
                ShapeType.SQUARE_2X2 -> listOf(Coordinate(0, 0), Coordinate(0, 1), Coordinate(1, 0), Coordinate(1, 1))
                ShapeType.SQUARE_3X3 -> listOf(
                    Coordinate(0, 0), Coordinate(0, 1), Coordinate(0, 2),
                    Coordinate(1, 0), Coordinate(1, 1), Coordinate(1, 2),
                    Coordinate(2, 0), Coordinate(2, 1), Coordinate(2, 2)
                )
                ShapeType.L_SMALL_TL -> listOf(Coordinate(0, 0), Coordinate(0, 1), Coordinate(1, 0))
                ShapeType.L_SMALL_TR -> listOf(Coordinate(0, 0), Coordinate(0, 1), Coordinate(1, 1))
                ShapeType.L_SMALL_BL -> listOf(Coordinate(0, 0), Coordinate(1, 0), Coordinate(1, 1))
                ShapeType.L_SMALL_BR -> listOf(Coordinate(0, 1), Coordinate(1, 0), Coordinate(1, 1))

                ShapeType.L_LARGE_TL -> listOf(Coordinate(0, 0), Coordinate(0, 1), Coordinate(0, 2), Coordinate(1, 0), Coordinate(2, 0))
                ShapeType.L_LARGE_TR -> listOf(Coordinate(0, 0), Coordinate(0, 1), Coordinate(0, 2), Coordinate(1, 2), Coordinate(2, 2))
                ShapeType.L_LARGE_BL -> listOf(Coordinate(0, 0), Coordinate(1, 0), Coordinate(2, 0), Coordinate(2, 1), Coordinate(2, 2))
                ShapeType.L_LARGE_BR -> listOf(Coordinate(0, 2), Coordinate(1, 2), Coordinate(2, 0), Coordinate(2, 1), Coordinate(2, 2))

                ShapeType.T_DOWN -> listOf(Coordinate(0, 0), Coordinate(0, 1), Coordinate(0, 2), Coordinate(1, 1))
                ShapeType.T_UP -> listOf(Coordinate(0, 1), Coordinate(1, 0), Coordinate(1, 1), Coordinate(1, 2))
                ShapeType.T_RIGHT -> listOf(Coordinate(0, 0), Coordinate(1, 0), Coordinate(2, 0), Coordinate(1, 1))
                ShapeType.T_LEFT -> listOf(Coordinate(0, 1), Coordinate(1, 0), Coordinate(1, 1), Coordinate(2, 1))

                ShapeType.Z_H -> listOf(Coordinate(0, 0), Coordinate(0, 1), Coordinate(1, 1), Coordinate(1, 2))
                ShapeType.Z_V -> listOf(Coordinate(0, 1), Coordinate(1, 0), Coordinate(1, 1), Coordinate(2, 0))
                ShapeType.S_H -> listOf(Coordinate(0, 1), Coordinate(0, 2), Coordinate(1, 0), Coordinate(1, 1))
                ShapeType.S_V -> listOf(Coordinate(0, 0), Coordinate(1, 0), Coordinate(1, 1), Coordinate(2, 1))

                ShapeType.CORNER_3_TL -> listOf(Coordinate(0, 0), Coordinate(0, 1), Coordinate(1, 0))
                ShapeType.CORNER_3_TR -> listOf(Coordinate(0, 0), Coordinate(0, 1), Coordinate(1, 1))
                ShapeType.CORNER_3_BL -> listOf(Coordinate(0, 0), Coordinate(1, 0), Coordinate(1, 1))
                ShapeType.CORNER_3_BR -> listOf(Coordinate(0, 1), Coordinate(1, 0), Coordinate(1, 1))

                ShapeType.PLUS -> listOf(
                    Coordinate(0, 1),
                    Coordinate(1, 0), Coordinate(1, 1), Coordinate(1, 2),
                    Coordinate(2, 1)
                )
                ShapeType.U_SHAPE -> listOf(
                    Coordinate(0, 0), Coordinate(0, 2),
                    Coordinate(1, 0), Coordinate(1, 1), Coordinate(1, 2)
                )
            }

            val maxR = coords.maxOf { it.r } + 1
            val maxC = coords.maxOf { it.c } + 1

            return BlockShape(
                type = type,
                color = color,
                coordinates = coords,
                rows = maxR,
                cols = maxC
            )
        }
    }
}
