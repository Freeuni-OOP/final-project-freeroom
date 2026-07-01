export const NOTIFICATION_PREFERENCE = Object.freeze({
    NONE: 'NONE',
    EMAIL: 'EMAIL',
    TELEGRAM: 'TELEGRAM',
});

export const UNIVERSITY = Object.freeze({
    FREEUNI: 'თავისუფალი',
    AGRUNI: 'აგრარული',
});

export const ROOM_STATUS = Object.freeze({
    OCCUPIED: 'occupied',
    FREE: 'free',
});

export const RELATIONSHIP_STATUS = Object.freeze({
    NONE: 'NONE',
    SELF: 'SELF',
    PENDING_SENT: 'PENDING_SENT',
    PENDING_RECEIVED: 'PENDING_RECEIVED',
    FRIENDS: 'FRIENDS',
});

export const REPORT_REASON = Object.freeze({
    INAPPROPRIATE_PHOTO: 'INAPPROPRIATE_PHOTO',
    INAPPROPRIATE_BIO: 'INAPPROPRIATE_BIO',
    OFFENSIVE_NAME: 'OFFENSIVE_NAME',
    HARASSMENT: 'HARASSMENT',
    OTHER: 'OTHER',
});

export const REPORT_REASON_LABELS = Object.freeze({
    [REPORT_REASON.INAPPROPRIATE_PHOTO]: 'შეუფერებელი პროფილის ფოტო',
    [REPORT_REASON.INAPPROPRIATE_BIO]: 'შეუფერებელი ბიოგრაფია',
    [REPORT_REASON.OFFENSIVE_NAME]: 'შეურაცხმყოფელი სახელი',
    [REPORT_REASON.HARASSMENT]: 'შევიწროება',
    [REPORT_REASON.OTHER]: 'სხვა',
});