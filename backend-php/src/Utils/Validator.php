<?php
declare(strict_types=1);

namespace App\Utils;

class Validator
{
    private array $errors = [];

    public function requireFields(array $data, array $requiredFields): self
    {
        foreach ($requiredFields as $field) {
            if (!isset($data[$field]) || trim((string)$data[$field]) === '') {
                $this->errors[$field] = ucfirst(str_replace('_', ' ', $field)) . ' is required.';
            }
        }
        return $this;
    }

    public function validateEmail(string $email, string $field = 'email'): self
    {
        if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            $this->errors[$field] = 'Please provide a valid email address.';
        }
        return $this;
    }

    public function validateMinLength(string $value, int $min, string $field): self
    {
        if (mb_strlen($value) < $min) {
            $this->errors[$field] = ucfirst(str_replace('_', ' ', $field)) . " must be at least {$min} characters.";
        }
        return $this;
    }

    public function validatePrice(mixed $price, string $field = 'price'): self
    {
        if (!is_numeric($price) || (float)$price < 0) {
            $this->errors[$field] = 'Price must be a valid positive number.';
        }
        return $this;
    }

    public function hasErrors(): bool
    {
        return !empty($this->errors);
    }

    public function getErrors(): array
    {
        return $this->errors;
    }

    public function getFirstError(): string
    {
        return !empty($this->errors) ? reset($this->errors) : '';
    }
}
