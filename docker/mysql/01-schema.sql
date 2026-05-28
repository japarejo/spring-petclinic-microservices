USE petclinic;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS diagnoses;
DROP TABLE IF EXISTS diseases_pet_typeswith_prevalence;
DROP TABLE IF EXISTS visits;
DROP TABLE IF EXISTS pets;
DROP TABLE IF EXISTS owners;
DROP TABLE IF EXISTS vet_specialties;
DROP TABLE IF EXISTS vets;
DROP TABLE IF EXISTS specialties;
DROP TABLE IF EXISTS diseases;
DROP TABLE IF EXISTS types;
DROP TABLE IF EXISTS authorities;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS researchers_phd_advisors;
DROP TABLE IF EXISTS researchers;
DROP TABLE IF EXISTS payment;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE users (
  username VARCHAR(64) NOT NULL,
  password VARCHAR(128) NOT NULL,
  enabled BOOLEAN NOT NULL,
  PRIMARY KEY (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE authorities (
  id INT NOT NULL AUTO_INCREMENT,
  version INT,
  username VARCHAR(64) NOT NULL,
  authority VARCHAR(50) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES users(username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE vets (
  id INT NOT NULL AUTO_INCREMENT,
  version INT,
  first_name VARCHAR(255),
  last_name VARCHAR(255),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE specialties (
  id INT NOT NULL AUTO_INCREMENT,
  version INT,
  name VARCHAR(50),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE vet_specialties (
  vet_id INT NOT NULL,
  specialty_id INT NOT NULL,
  PRIMARY KEY (vet_id, specialty_id),
  CONSTRAINT fk_vet_specialties_vets FOREIGN KEY (vet_id) REFERENCES vets(id),
  CONSTRAINT fk_vet_specialties_specialties FOREIGN KEY (specialty_id) REFERENCES specialties(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE types (
  id INT NOT NULL AUTO_INCREMENT,
  version INT,
  name VARCHAR(50),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE diseases (
  id INT NOT NULL AUTO_INCREMENT,
  version INT,
  name VARCHAR(50),
  description VARCHAR(1024),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE diseases_pet_typeswith_prevalence (
  disease_id INT NOT NULL,
  pet_typeswith_prevalence_id INT NOT NULL,
  PRIMARY KEY (disease_id, pet_typeswith_prevalence_id),
  CONSTRAINT fk_diseases_pet_types_diseases FOREIGN KEY (disease_id) REFERENCES diseases(id),
  CONSTRAINT fk_diseases_pet_types_types FOREIGN KEY (pet_typeswith_prevalence_id) REFERENCES types(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE owners (
  id INT NOT NULL AUTO_INCREMENT,
  version INT,
  first_name VARCHAR(255),
  last_name VARCHAR(255),
  address VARCHAR(255),
  city VARCHAR(255),
  telephone VARCHAR(255),
  username VARCHAR(64),
  PRIMARY KEY (id),
  UNIQUE KEY uk_owners_username (username),
  CONSTRAINT fk_owners_users FOREIGN KEY (username) REFERENCES users(username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pets (
  id INT NOT NULL AUTO_INCREMENT,
  version INT,
  name VARCHAR(50),
  birth_date DATE,
  age INT,
  type_id INT,
  owner_id INT,
  PRIMARY KEY (id),
  CONSTRAINT fk_pets_types FOREIGN KEY (type_id) REFERENCES types(id),
  CONSTRAINT fk_pets_owners FOREIGN KEY (owner_id) REFERENCES owners(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE visits (
  id INT NOT NULL AUTO_INCREMENT,
  version INT,
  pet_id INT,
  visit_date DATE,
  description VARCHAR(255),
  PRIMARY KEY (id),
  CONSTRAINT fk_visits_pets FOREIGN KEY (pet_id) REFERENCES pets(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE diagnoses (
  id INT NOT NULL AUTO_INCREMENT,
  version INT,
  visit_id INT NOT NULL,
  disease_id INT NOT NULL,
  vet_id INT NOT NULL,
  description VARCHAR(1024),
  PRIMARY KEY (id),
  UNIQUE KEY uk_diagnoses_visit (visit_id),
  CONSTRAINT fk_diagnoses_visits FOREIGN KEY (visit_id) REFERENCES visits(id),
  CONSTRAINT fk_diagnoses_diseases FOREIGN KEY (disease_id) REFERENCES diseases(id),
  CONSTRAINT fk_diagnoses_vets FOREIGN KEY (vet_id) REFERENCES vets(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE researchers (
  id INT NOT NULL AUTO_INCREMENT,
  version INT,
  name VARCHAR(50),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE researchers_phd_advisors (
  researcher_id INT NOT NULL,
  phd_advisors_id INT NOT NULL,
  PRIMARY KEY (researcher_id, phd_advisors_id),
  CONSTRAINT fk_researchers_phd_students FOREIGN KEY (researcher_id) REFERENCES researchers(id),
  CONSTRAINT fk_researchers_phd_advisors FOREIGN KEY (phd_advisors_id) REFERENCES researchers(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE payment (
  id INT NOT NULL AUTO_INCREMENT,
  version INT,
  amount DOUBLE NOT NULL,
  owner_id INT,
  creator VARCHAR(255),
  created_date DATETIME(6),
  modifier VARCHAR(255),
  last_modified_date DATETIME(6),
  PRIMARY KEY (id),
  CONSTRAINT fk_payment_owners FOREIGN KEY (owner_id) REFERENCES owners(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
