import { UNIVERSITY } from './constants';

const UNIVERSITY_BY_DOMAIN = {
    '@freeuni.edu.ge': UNIVERSITY.FREEUNI,
    '@agruni.edu.ge': UNIVERSITY.AGRUNI,
};

export const getUniversity = (email) => {
    const normalized = email?.toLowerCase() || '';
    const match = Object.entries(UNIVERSITY_BY_DOMAIN).find(([domain]) => normalized.endsWith(domain));
    return match ? match[1] : null;
};