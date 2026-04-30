import { useState } from 'react';
import { Search, Users, Stethoscope, ToggleLeft, ToggleRight } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card';
import { Input } from '../../components/ui/input';
import { Badge } from '../../components/ui/badge';
import { Button } from '../../components/ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../../components/ui/tabs';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table';
import { customers, vets } from '../../lib/data';
import { toast } from 'sonner';

export function ManageUsers() {
  const [searchTerm, setSearchTerm] = useState('');
  const [customerList, setCustomerList] = useState(customers);
  const [vetList, setVetList] = useState(vets);

  const toggleUserStatus = (userId: string, userType: 'customer' | 'vet') => {
    if (userType === 'customer') {
      setCustomerList(prev =>
        prev.map(user =>
          user.id === userId ? { ...user, isActive: !user.isActive } : user
        )
      );
    } else {
      setVetList(prev =>
        prev.map(user =>
          user.id === userId ? { ...user, isActive: !user.isActive } : user
        )
      );
    }
    toast.success('User status updated');
  };

  const filteredCustomers = customerList.filter(customer =>
    `${customer.firstName} ${customer.lastName} ${customer.email}`
      .toLowerCase()
      .includes(searchTerm.toLowerCase())
  );

  const filteredVets = vetList.filter(vet =>
    `${vet.firstName} ${vet.lastName} ${vet.email} ${vet.specialization}`
      .toLowerCase()
      .includes(searchTerm.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div>
        <h1 className="mb-2">Manage Users</h1>
        <p className="text-muted-foreground">View and manage customers and veterinarians</p>
      </div>

      {/* Search */}
      <Card>
        <CardContent className="pt-6">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search by name, email, or specialization..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>
        </CardContent>
      </Card>

      {/* User Tables */}
      <Tabs defaultValue="customers" className="w-full">
        <TabsList className="grid w-full max-w-md grid-cols-2">
          <TabsTrigger value="customers">
            <Users className="h-4 w-4 mr-2" />
            Customers ({customerList.length})
          </TabsTrigger>
          <TabsTrigger value="vets">
            <Stethoscope className="h-4 w-4 mr-2" />
            Veterinarians ({vetList.length})
          </TabsTrigger>
        </TabsList>

        <TabsContent value="customers" className="mt-6">
          <Card>
            <CardHeader>
              <CardTitle>Customer Accounts</CardTitle>
              <CardDescription>Manage customer accounts and permissions</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="rounded-md border">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Name</TableHead>
                      <TableHead>Email</TableHead>
                      <TableHead>Phone</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead className="text-right">Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {filteredCustomers.map((customer) => (
                      <TableRow key={customer.id}>
                        <TableCell className="font-medium">
                          {customer.firstName} {customer.lastName}
                        </TableCell>
                        <TableCell>{customer.email}</TableCell>
                        <TableCell>{customer.phone}</TableCell>
                        <TableCell>
                          <Badge variant={customer.isActive ? 'default' : 'secondary'}>
                            {customer.isActive ? 'Active' : 'Disabled'}
                          </Badge>
                        </TableCell>
                        <TableCell className="text-right">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => toggleUserStatus(customer.id, 'customer')}
                          >
                            {customer.isActive ? (
                              <>
                                <ToggleRight className="h-4 w-4 mr-2" />
                                Disable
                              </>
                            ) : (
                              <>
                                <ToggleLeft className="h-4 w-4 mr-2" />
                                Enable
                              </>
                            )}
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
              {filteredCustomers.length === 0 && (
                <div className="text-center py-8 text-muted-foreground">
                  No customers found
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="vets" className="mt-6">
          <Card>
            <CardHeader>
              <CardTitle>Veterinarian Accounts</CardTitle>
              <CardDescription>Manage veterinarian accounts and permissions</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="rounded-md border">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Name</TableHead>
                      <TableHead>Specialization</TableHead>
                      <TableHead>Experience</TableHead>
                      <TableHead>Email</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead className="text-right">Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {filteredVets.map((vet) => (
                      <TableRow key={vet.id}>
                        <TableCell className="font-medium">
                          {vet.firstName} {vet.lastName}
                        </TableCell>
                        <TableCell>{vet.specialization}</TableCell>
                        <TableCell>{vet.experience} years</TableCell>
                        <TableCell>{vet.email}</TableCell>
                        <TableCell>
                          <Badge variant={vet.isActive ? 'default' : 'secondary'}>
                            {vet.isActive ? 'Active' : 'Disabled'}
                          </Badge>
                        </TableCell>
                        <TableCell className="text-right">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => toggleUserStatus(vet.id, 'vet')}
                          >
                            {vet.isActive ? (
                              <>
                                <ToggleRight className="h-4 w-4 mr-2" />
                                Disable
                              </>
                            ) : (
                              <>
                                <ToggleLeft className="h-4 w-4 mr-2" />
                                Enable
                              </>
                            )}
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
              {filteredVets.length === 0 && (
                <div className="text-center py-8 text-muted-foreground">
                  No veterinarians found
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
