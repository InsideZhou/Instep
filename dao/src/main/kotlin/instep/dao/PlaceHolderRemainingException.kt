package instep.dao

class PlaceHolderRemainingException(msg: String) : DaoException(msg) {
    constructor(placeHolder: PlaceHolder) : this("${placeHolder.index}-${placeHolder.name} is remaining.")
}