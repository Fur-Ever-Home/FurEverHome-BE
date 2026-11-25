-- These tables are queried with JDBC and are not created by Hibernate.
CREATE TABLE IF NOT EXISTS tratamente (
    id_tratament INTEGER PRIMARY KEY,
    nume_tratament VARCHAR(100),
    categorie VARCHAR(100),
    descriere TEXT
);

CREATE TABLE IF NOT EXISTS inregistrari_medicale (
    nr_fisa BIGINT REFERENCES fisa_medicala(nr_fisa),
    id_tratament INTEGER REFERENCES tratamente(id_tratament),
    data_inregistrarii DATE,
    PRIMARY KEY (nr_fisa, id_tratament)
);

INSERT INTO animale (
    nume, specie, rasa, varsta, sex, talie, status, tip_locatie, nume_centru,
    data_adaugare, temperament, tip_adoptie, descriere_ro, descriere_en
)
SELECT sample.name, sample.species, sample.breed, sample.age, sample.gender,
       sample.size, 'disponibil', 'adapost', 'Adăpost demo', CURRENT_TIMESTAMP,
       sample.temperament, 'fizica', sample.description_ro, sample.description_en
FROM (VALUES
    ('Luna (demo)', 'Pisică', 'Europeană', '2 ani', 'Femelă', 'Mică', 'Blândă',
     'Profil fictiv pentru demonstrația catalogului.', 'Fictional profile for the catalog demo.'),
    ('Max (demo)', 'Câine', 'Metis', '3 ani', 'Mascul', 'Medie', 'Jucăuș',
     'Profil fictiv pentru demonstrația catalogului.', 'Fictional profile for the catalog demo.')
) AS sample(name, species, breed, age, gender, size, temperament, description_ro, description_en)
WHERE NOT EXISTS (
    SELECT 1 FROM animale WHERE animale.nume = sample.name
);
