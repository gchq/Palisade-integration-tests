/*
 * Copyright 2020 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.palisade.integrationtests.data.mock;

import com.github.javafaker.Faker;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Address;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.BankDetails;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Department;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.EmergencyContact;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Grade;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Manager;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Nationality;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.PhoneNumber;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Sex;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.WorkLocation;

import java.util.Random;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class DataServiceMock {

    public static WireMockRule getRule() {
        return new WireMockRule(options().port(8082).notifier(new ConsoleNotifier(true)));
    }

    public static Employee testEmployee() {
        Employee employee = new Employee();
        BankDetails bankDetails = new BankDetails();
        bankDetails.setAccountNumber("85346502");
        bankDetails.setSortCode("421122");

        employee.setUid(new UserId().id("1292567335"));
        employee.setName("Delia Carter");
        employee.setAddress(createAddress());
        employee.setBankDetails(bankDetails);
        employee.setContactNumbers(PhoneNumber.generateMany(new Random(1)));
        employee.setDateOfBirth("19/2/1803");
        employee.setDepartment(Department.Chief_Data_Office);
        employee.setEmergencyContacts(EmergencyContact.generateMany(new Faker(), new Random(1)));
        employee.setGrade(Grade.Grade6);
        employee.setHireDate("19/2/1852");
        employee.setManager(Manager.generateMany(new Random(1), 3));
        employee.setNationality(Nationality.Salvadorean);
        employee.setSalaryAmount(259820);
        employee.setSalaryBonus(6971);
        employee.setSex(Sex.Not_Specified);
        employee.setTaxCode("");
        employee.setWorkLocation(WorkLocation.generate(new Faker(), new Random(1)));

        return employee;
    }

    private static Address createAddress() {
        Address address = new Address();
        address.setCity("New Ray");
        address.setState("Oregon");
        address.setStreetAddressNumber("602");
        address.setStreetName("Hilll Burgs");
        address.setZipCode("CF0H 8LY");

        return address;
    }
}
