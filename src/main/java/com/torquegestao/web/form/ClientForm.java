package com.torquegestao.web.form;

import com.torquegestao.domain.Client;
import com.torquegestao.dto.ClientRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientForm {

    @NotBlank(message = "Nome é obrigatório")
    private String name;
    private String cpf;
    private String phone;
    @Email(message = "Email inválido")
    private String email;
    private String address;

    public static ClientForm from(Client c) {
        ClientForm f = new ClientForm();
        f.name = c.getName();
        f.cpf = c.getCpf();
        f.phone = c.getPhone();
        f.email = c.getEmail();
        f.address = c.getAddress();
        return f;
    }

    public ClientRequest toRequest() {
        return new ClientRequest(name, cpf, phone, email, address);
    }
}
