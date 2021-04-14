class Common {
    static getOrdinal(number) {
        let ordinal = "th";
        if (number % 10 == 1 && number % 100 != 11) {
            ordinal = "st";
        } else if (number % 10 == 2 && number % 100 != 12) {
            ordinal = "nd";
        } else if (number % 10 == 3 && number % 100 != 13) {
            ordinal = "rd";
        }
        return number + ordinal;
    }
}

export default Common;