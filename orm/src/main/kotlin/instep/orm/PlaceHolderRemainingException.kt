package instep.orm

class PlaceHolderRemainingException(msg: String) : OrmException(msg) {
    constructor(placeHolder: PlaceHolder) : this("${placeHolder.index}-${placeHolder.name} is remaining.")
}