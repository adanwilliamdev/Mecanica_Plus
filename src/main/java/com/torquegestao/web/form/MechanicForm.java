package com.torquegestao.web.form;

import com.torquegestao.domain.Mechanic;
import com.torquegestao.dto.MechanicRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MechanicForm {

    @NotBlank(message = "Nome é obrigatório")
    private String name;
    private String specialty;
    private String phone;

    public static MechanicForm from(Mechanic m) {
        MechanicForm f = new MechanicForm();
        f.name = m.getName();
        f.specialty = m.getSpecialty();
        f.phone = m.getPhone();
        return f;
    }

    public MechanicRequest toRequest() {
        return new MechanicRequest(name, specialty, phone, null);
    }
}
