package instep.orm

import instep.orm.OrmException
import instep.orm.PlaceHolder

class PlaceHolderRemainingException(val placeHolder: PlaceHolder) : OrmException("${placeHolder.index}-${placeHolder.name} is remaining.") {
}