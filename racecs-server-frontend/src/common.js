import numeral from 'numeral';

class Common {
    static getOrdinal(number) {
        return numeral(number).format("0o");
    }
}

export default Common;