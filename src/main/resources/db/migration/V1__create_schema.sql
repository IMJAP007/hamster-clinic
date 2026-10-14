-- Владельцы хомяков
CREATE TABLE owners (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(200) NOT NULL,
    phone VARCHAR(30),
    email VARCHAR(150),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    
    CONSTRAINT uq_owners_email UNIQUE (email)
);


-- Хомяки
CREATE TABLE hamsters (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    owner_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    
    CONSTRAINT fk_hamsters_owner FOREIGN KEY (owner_id) REFERENCES owners (id) ON DELETE RESTRICT
);

CREATE INDEX idx_hamsters_owner_id ON hamsters (owner_id);


-- Врачи
CREATE TABLE doctors (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(200) NOT NULL,
    specialty VARCHAR(100) NOT NULL,
    phone VARCHAR(30),
    email VARCHAR(150),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    
    CONSTRAINT uq_doctors_email UNIQUE (email)
);


-- Болезни
CREATE TABLE diseases (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    
    CONSTRAINT uq_diseases_name UNIQUE (name)
);


-- Записи на приём
CREATE TABLE appointments (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    hamster_id BIGINT NOT NULL,
    appointment_date TIMESTAMP NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    
    CONSTRAINT fk_appointments_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id) ON DELETE RESTRICT,
    CONSTRAINT fk_appointments_owner FOREIGN KEY (owner_id) REFERENCES owners (id) ON DELETE RESTRICT,
    CONSTRAINT fk_appointments_hamster FOREIGN KEY (hamster_id) REFERENCES hamsters (id) ON DELETE CASCADE,
    CONSTRAINT uq_appointments_doctor_datetime UNIQUE (doctor_id, appointment_date)
);

CREATE INDEX idx_appointments_hamster_id ON appointments (hamster_id);
CREATE INDEX idx_appointments_doctor_id ON appointments (doctor_id);
CREATE INDEX idx_appointments_owner_id ON appointments (owner_id);


-- Болезни хомяков
CREATE TABLE hamster_diseases (
    id BIGSERIAL PRIMARY KEY,
    hamster_id BIGINT NOT NULL,
    disease_id BIGINT NOT NULL,
    diagnosed_at DATE NOT NULL,
    
    CONSTRAINT fk_hd_hamster FOREIGN KEY (hamster_id) REFERENCES hamsters (id) ON DELETE CASCADE,
    CONSTRAINT fk_hd_disease FOREIGN KEY (disease_id) REFERENCES diseases (id) ON DELETE RESTRICT,
    CONSTRAINT uq_hd_hamster_disease_date UNIQUE (hamster_id, disease_id, diagnosed_at)
);

CREATE INDEX idx_hd_hamster_id ON hamster_diseases (hamster_id);
CREATE INDEX idx_hd_disease_id ON hamster_diseases (disease_id);