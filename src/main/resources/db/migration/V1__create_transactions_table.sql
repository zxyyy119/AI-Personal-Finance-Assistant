CREATE TABLE transactions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    type VARCHAR(20) NOT NULL,
    category VARCHAR(30) NOT NULL,
    description VARCHAR(500),
    amount DECIMAL(12, 2) NOT NULL,
    transaction_date DATE NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;
